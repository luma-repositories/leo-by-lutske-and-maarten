package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.service.ExtractionResult;
import be.lutske.leolegacy.application.service.RecipeExtractionException;
import be.lutske.leolegacy.application.service.RecipeExtractionService;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ImportConfirmRequest;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ImportExtractionResponse;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ProposedRecipeDto;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST resource for importing recipes from images via AI-powered extraction.
 *
 * <p>Flow:
 * <ol>
 *   <li>{@code POST /api/recipes/import} — always returns HTTP 200 with extraction for user review</li>
 *   <li>{@code POST /api/recipes/import/confirm} — saves the recipe after user review/correction</li>
 * </ol>
 */
@Path("/api/recipes/import")
public class RecipeImportResource {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final long DEFAULT_IMPORT_CATEGORY_ID = 15L; // "Imported"
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final RecipeExtractionService extractionService;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public RecipeImportResource(RecipeExtractionService extractionService,
                                RecipeRepository recipeRepository,
                                CategoryRepository categoryRepository) {
        this.extractionService = extractionService;
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Upload an image and extract a recipe via AI (multimodal LLM).
     * Always returns HTTP 200 with the extraction result for user review.
     * The recipe is NOT saved — use {@code /confirm} to save after review.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importFromImage(@RestForm("file") FileUpload file) {
        validateFile(file);

        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to read uploaded file: " + e.getMessage()))
                    .build();
        }

        String mimeType = file.contentType() != null ? file.contentType() : "image/png";

        ExtractionResult extraction;
        try {
            extraction = extractionService.extractRecipeFromImage(imageBytes, mimeType);
        } catch (RecipeExtractionException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "AI extraction failed: " + e.getMessage()))
                    .build();
        }

        var proposed = toProposedDto(extraction);
        String status = extraction.isComplete() ? "COMPLETE" : "NEEDS_MORE_INFO";

        return Response.ok(new ImportExtractionResponse(
                status,
                extraction.rawModelResponse(),
                proposed,
                extraction.missingFields(),
                extraction.warnings()
        )).build();
    }

    /**
     * Confirm and finalize an imported recipe after user review.
     */
    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response confirmImport(ImportConfirmRequest request) {
        var proposed = request.proposedRecipe();
        var overrides = request.userOverrides();

        String title = override(overrides != null ? overrides.title() : null, proposed.title());
        List<String> ingredients = override(overrides != null ? overrides.ingredients() : null, proposed.ingredients());
        String preparation = override(overrides != null ? overrides.preparation() : null, proposed.preparation());
        Long categoryId = override(overrides != null ? overrides.categoryId() : null, proposed.categoryId());
        String notes = overrides != null ? overrides.notes() : proposed.notes();

        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title is required");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new BadRequestException("Ingredients are required");
        }
        if (preparation == null || preparation.isBlank()) {
            throw new BadRequestException("Preparation is required");
        }

        String finalPreparation = (notes != null && !notes.isBlank())
                ? preparation + "\n\nNotes: " + notes
                : preparation;

        var merged = new ProposedRecipeDto(
                title, proposed.description(), proposed.servings(),
                ingredients, finalPreparation, proposed.source(), proposed.tags(),
                categoryId != null ? categoryId : DEFAULT_IMPORT_CATEGORY_ID,
                null
        );

        var recipe = createRecipeFromProposal(merged, request.rawModelResponse());
        return Response.status(Response.Status.CREATED)
                .entity(toDetailResponse(recipe))
                .build();
    }

    private <T> T override(T overrideValue, T originalValue) {
        return overrideValue != null ? overrideValue : originalValue;
    }

    private void validateFile(FileUpload file) {
        long fileSize = file.uploadedFile().toFile().length();
        if (fileSize > MAX_FILE_SIZE) {
            throw new BadRequestException("File too large: " + (fileSize / 1024 / 1024) + " MB. Maximum is 10 MB.");
        }

        String contentType = file.contentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported file type: " + contentType + ". Allowed: PNG, JPG, JPEG, WEBP.");
        }

        String fileName = file.fileName();
        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file extension: ." + extension + ". Allowed: .png, .jpg, .jpeg, .webp.");
        }
    }

    /**
     * Convert ExtractionResult to ProposedRecipeDto.
     * Steps (list) are joined into a single preparation string to match the frontend contract.
     */
    private ProposedRecipeDto toProposedDto(ExtractionResult extraction) {
        String preparation = extraction.steps() != null
                ? String.join("\n", extraction.steps())
                : null;

        return new ProposedRecipeDto(
                extraction.title(),
                extraction.description(),
                extraction.servings(),
                extraction.ingredients(),
                preparation,
                extraction.source(),
                extraction.tags(),
                null, // categoryId — set on confirm
                null  // notes
        );
    }

    private RecipeEntity createRecipeFromProposal(ProposedRecipeDto proposal, String rawModelResponse) {
        long categoryId = proposal.categoryId() != null ? proposal.categoryId() : DEFAULT_IMPORT_CATEGORY_ID;
        var category = categoryRepository.findById(categoryId);
        if (category == null) {
            category = categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID);
        }
        if (category == null) {
            throw new IllegalStateException("Default import category not found");
        }

        var recipe = new RecipeEntity();
        recipe.setTitle(proposal.title() != null ? proposal.title() : "Untitled Recipe");
        recipe.setIngredients(proposal.ingredients() != null ? String.join("|", proposal.ingredients()) : "");
        recipe.setPreparation(proposal.preparation() != null ? proposal.preparation() : "");
        recipe.setCategory(category);
        recipe.setViewCount(0);
        recipe.setSource(proposal.source() != null ? proposal.source() : "Imported from image");
        recipe.setCreatedAt(Instant.now());

        if (rawModelResponse != null) {
            recipe.setImportMetadata(rawModelResponse);
        }

        recipeRepository.persist(recipe);
        return recipe;
    }

    private RecipeDetailResponse toDetailResponse(RecipeEntity entity) {
        List<String> ingredientList = Arrays.stream(entity.getIngredients().split("\\|"))
                .filter(s -> !s.isBlank())
                .toList();
        return new RecipeDetailResponse(
                entity.getId(),
                entity.getTitle(),
                ingredientList,
                entity.getPreparation(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getViewCount()
        );
    }
}
