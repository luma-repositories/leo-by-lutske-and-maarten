package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.service.OcrException;
import be.lutske.leolegacy.application.service.OcrService;
import be.lutske.leolegacy.application.service.RecipeParserService;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ImportConfirmRequest;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ImportNeedsMoreInfoResponse;
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
 * REST resource for importing recipes from images via OCR.
 */
@Path("/api/recipes/import")
public class RecipeImportResource {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final long DEFAULT_IMPORT_CATEGORY_ID = 15L; // "Geimporteerd"
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final OcrService ocrService;
    private final RecipeParserService parserService;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public RecipeImportResource(OcrService ocrService, RecipeParserService parserService,
                                RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.ocrService = ocrService;
        this.parserService = parserService;
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Upload an image and attempt to extract a recipe via OCR.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
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

        String rawText;
        try {
            rawText = ocrService.extractText(imageBytes);
        } catch (OcrException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "OCR failed: " + e.getMessage()))
                    .build();
        }

        var parseResult = parserService.parse(rawText);

        if (parseResult.missingFields().isEmpty()) {
            var recipe = createRecipeFromProposal(parseResult.proposedRecipe());
            return Response.status(Response.Status.CREATED)
                    .entity(toDetailResponse(recipe))
                    .build();
        }

        return Response.status(422)
                .entity(new ImportNeedsMoreInfoResponse(
                        rawText,
                        parseResult.proposedRecipe(),
                        parseResult.missingFields(),
                        parseResult.parseWarnings()
                ))
                .build();
    }

    /**
     * Confirm and finalize an imported recipe after user provides missing info.
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
                ? preparation + "\n\nNotities: " + notes
                : preparation;

        var mergedProposal = new ProposedRecipeDto(
                title, ingredients, finalPreparation,
                categoryId != null ? categoryId : DEFAULT_IMPORT_CATEGORY_ID, null
        );

        var recipe = createRecipeFromProposal(mergedProposal);
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

    private RecipeEntity createRecipeFromProposal(ProposedRecipeDto proposal) {
        long categoryId = proposal.categoryId() != null ? proposal.categoryId() : DEFAULT_IMPORT_CATEGORY_ID;
        var category = categoryRepository.findById(categoryId);
        if (category == null) {
            category = categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID);
        }
        if (category == null) {
            throw new IllegalStateException("Default import category not found");
        }

        var recipe = new RecipeEntity();
        recipe.setTitle(proposal.title() != null ? proposal.title() : "Naamloos recept");
        recipe.setIngredients(proposal.ingredients() != null ? String.join("|", proposal.ingredients()) : "");
        recipe.setPreparation(proposal.preparation() != null ? proposal.preparation() : "");
        recipe.setCategory(category);
        recipe.setViewCount(0);
        recipe.setSource("Imported from image");
        recipe.setCreatedAt(Instant.now());

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
