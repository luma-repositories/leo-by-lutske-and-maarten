package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.domain.recipe.RecipeExtraction;
import be.lutske.leolegacy.domain.recipe.RecipeExtractionException;
import be.lutske.leolegacy.usecase.recipe.ConfirmRecipeImportUseCase;
import be.lutske.leolegacy.usecase.recipe.ExtractRecipeFromImageUseCase;
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
import java.util.Map;
import java.util.Set;

@Path("/api/recipes/import")
public class RecipeImportResource {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final ExtractRecipeFromImageUseCase extractRecipeFromImageUseCase;
    private final ConfirmRecipeImportUseCase confirmRecipeImportUseCase;

    public RecipeImportResource(ExtractRecipeFromImageUseCase extractRecipeFromImageUseCase,
                                ConfirmRecipeImportUseCase confirmRecipeImportUseCase) {
        this.extractRecipeFromImageUseCase = extractRecipeFromImageUseCase;
        this.confirmRecipeImportUseCase = confirmRecipeImportUseCase;
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
            extraction = extractRecipeFromImageUseCase.execute(imageBytes, mimeType);
        } catch (RecipeExtractionException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "AI extraction failed: " + e.getMessage()))
                    .build();
        }

        return Response.ok(new RecipeImportDtos.ImportExtractionResponse(
                extraction.isComplete() ? "COMPLETE" : "NEEDS_MORE_INFO",
                extraction.rawModelResponse(),
                RestRecipeMapper.toProposedRecipeDto(extraction),
                extraction.missingFields(),
                extraction.warnings()
        )).build();
    }

    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response confirmImport(RecipeImportDtos.ImportConfirmRequest request) {
        try {
            var recipe = confirmRecipeImportUseCase.execute(RestRecipeMapper.toConfirmCommand(request));
            return Response.status(Response.Status.CREATED)
                    .entity(RestRecipeMapper.toDetailResponse(recipe))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
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
}
