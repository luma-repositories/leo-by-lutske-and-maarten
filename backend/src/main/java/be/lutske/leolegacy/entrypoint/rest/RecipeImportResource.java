package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.domain.exception.RecipeExtractionException;
import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.model.RecipeExtraction;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeDetailResponse;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeImportDtos.ImportConfirmRequest;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeImportDtos.ImportExtractionResponse;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeImportDtos.ProposedRecipeDto;
import be.lutske.leolegacy.usecase.ConfirmRecipeImport;
import be.lutske.leolegacy.usecase.ExtractRecipeFromImage;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/api/recipes/import")
public class RecipeImportResource {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final ExtractRecipeFromImage extractRecipeFromImage;
    private final ConfirmRecipeImport confirmRecipeImport;

    public RecipeImportResource(ExtractRecipeFromImage extractRecipeFromImage,
                                ConfirmRecipeImport confirmRecipeImport) {
        this.extractRecipeFromImage = extractRecipeFromImage;
        this.confirmRecipeImport = confirmRecipeImport;
    }

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

        RecipeExtraction extraction;
        try {
            extraction = extractRecipeFromImage.execute(imageBytes, mimeType);
        } catch (RecipeExtractionException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "AI extraction failed: " + e.getMessage()))
                    .build();
        }

        var proposed = toProposedDto(extraction);
        String status = extraction.isComplete() ? "COMPLETE" : "NEEDS_MORE_INFO";

        return Response.ok(new ImportExtractionResponse(
                status, extraction.rawModelResponse(), proposed,
                extraction.missingFields(), extraction.warnings()
        )).build();
    }

    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmImport(ImportConfirmRequest request) {
        var proposed = request.proposedRecipe();
        var overrides = request.userOverrides();

        String title = override(overrides != null ? overrides.title() : null, proposed.title());
        List<String> ingredients = override(overrides != null ? overrides.ingredients() : null, proposed.ingredients());
        String preparation = override(overrides != null ? overrides.preparation() : null, proposed.preparation());
        Long categoryId = override(overrides != null ? overrides.categoryId() : null, proposed.categoryId());
        String notes = overrides != null ? overrides.notes() : proposed.notes();

        String finalPreparation = (notes != null && !notes.isBlank())
                ? preparation + "\n\nNotes: " + notes
                : preparation;

        try {
            Recipe saved = confirmRecipeImport.execute(new ConfirmRecipeImport.ConfirmRequest(
                    title, ingredients, finalPreparation,
                    proposed.source(), categoryId,
                    request.rawModelResponse()
            ));
            return Response.status(Response.Status.CREATED)
                    .entity(toDetailResponse(saved))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
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
            throw new BadRequestException("Unsupported file type: " + contentType);
        }
        String fileName = file.fileName();
        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file extension: ." + extension);
        }
    }

    private ProposedRecipeDto toProposedDto(RecipeExtraction extraction) {
        String preparation = extraction.steps() != null
                ? String.join("\n", extraction.steps()) : null;
        return new ProposedRecipeDto(
                extraction.title(), extraction.description(), extraction.servings(),
                extraction.ingredients(), preparation,
                extraction.source(), extraction.tags(), null, null);
    }

    private RecipeDetailResponse toDetailResponse(Recipe recipe) {
        return new RecipeDetailResponse(
                recipe.getId(), recipe.getTitle(), recipe.getIngredients(),
                recipe.getPreparation(),
                recipe.getCategory() != null ? recipe.getCategory().getId() : 0,
                recipe.getCategory() != null ? recipe.getCategory().getName() : null,
                recipe.getViewCount());
    }
}
