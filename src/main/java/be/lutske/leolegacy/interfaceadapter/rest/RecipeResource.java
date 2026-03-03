package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.GetRecipeDetailUseCase;
import be.lutske.leolegacy.application.usecase.ImportRecipeFromImageUseCase;
import be.lutske.leolegacy.application.usecase.ListRecipesUseCase;
import be.lutske.leolegacy.application.usecase.ConfirmImportedRecipeUseCase;
import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeImportAnalysis;
import be.lutske.leolegacy.domain.RecipeImportOutcome;
import be.lutske.leolegacy.domain.RecipeSummary;
import be.lutske.leolegacy.domain.UserRecipeOverrides;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
public class RecipeResource {

    private final ListRecipesUseCase listRecipesUseCase;
    private final GetRecipeDetailUseCase getRecipeDetailUseCase;
    private final ImportRecipeFromImageUseCase importRecipeFromImageUseCase;
    private final ConfirmImportedRecipeUseCase confirmImportedRecipeUseCase;
    private final long maxFileSize;
    private final Set<String> allowedMimeTypes;
    private final Set<String> allowedExtensions;

    public RecipeResource(
            ListRecipesUseCase listRecipesUseCase,
            GetRecipeDetailUseCase getRecipeDetailUseCase,
            ImportRecipeFromImageUseCase importRecipeFromImageUseCase,
            ConfirmImportedRecipeUseCase confirmImportedRecipeUseCase,
            @ConfigProperty(name = "app.import.max-file-size-bytes", defaultValue = "8388608") long maxFileSize,
            @ConfigProperty(name = "app.import.allowed-mime-types", defaultValue = "image/png,image/jpeg,image/jpg,image/webp") String allowedMimeTypes,
            @ConfigProperty(name = "app.import.allowed-extensions", defaultValue = "png,jpg,jpeg,webp") String allowedExtensions
    ) {
        this.listRecipesUseCase = listRecipesUseCase;
        this.getRecipeDetailUseCase = getRecipeDetailUseCase;
        this.importRecipeFromImageUseCase = importRecipeFromImageUseCase;
        this.confirmImportedRecipeUseCase = confirmImportedRecipeUseCase;
        this.maxFileSize = maxFileSize;
        this.allowedMimeTypes = splitCsv(allowedMimeTypes);
        this.allowedExtensions = splitCsv(allowedExtensions);
    }

    @GET
    public List<RecipeSummaryResponse> listRecipes(@QueryParam("category") String categorySlug) {
        return listRecipesUseCase.execute(categorySlug).stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public RecipeDetailResponse getRecipe(@PathParam("id") Long id) {
        RecipeDetail detail = getRecipeDetailUseCase.execute(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
        return toDetailResponse(detail);
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importRecipe(@MultipartForm RecipeImportForm form) {
        FileUpload file = form == null ? null : form.file;
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("File is required."))
                    .build();
        }

        String filename = file.fileName() == null ? "" : file.fileName();
        String contentType = file.contentType() == null ? "" : file.contentType().toLowerCase(Locale.ROOT);
        if (!contentType.isBlank() && !allowedMimeTypes.contains(contentType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Unsupported file type."))
                    .build();
        }

        if (!hasAllowedExtension(filename)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Unsupported file extension."))
                    .build();
        }

        if (file.size() > maxFileSize) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("File too large."))
                    .build();
        }

        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiErrorResponse("Failed to read uploaded file."))
                    .build();
        }

        try {
            RecipeImportOutcome outcome = importRecipeFromImageUseCase.execute(imageBytes);
            if (outcome.needsMoreInfo()) {
                return Response.status(422)
                        .entity(toNeedsMoreInfoResponse(outcome.analysis()))
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(toDetailResponse(outcome.createdRecipe()))
                    .build();
        } catch (IllegalStateException e) {
            if ("OCR_RUNTIME_UNAVAILABLE".equals(e.getMessage())) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new ApiErrorResponse("OCR runtime unavailable. Install native tesseract and restart the backend."))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiErrorResponse("OCR failed to process the image."))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse(e.getMessage()))
                    .build();
        } catch (LinkageError e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ApiErrorResponse("OCR runtime unavailable. Install native tesseract and restart the backend."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiErrorResponse("OCR failed to process the image."))
                    .build();
        }
    }

    @POST
    @Path("/import/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmImport(RecipeImportConfirmRequest request) {
        if (request == null || request.proposedRecipe() == null || request.userOverrides() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Invalid import confirmation payload."))
                    .build();
        }

        RecipeImportOutcome outcome = confirmImportedRecipeUseCase.execute(
                request.rawText(),
                toProposedRecipe(request.proposedRecipe()),
                toUserOverrides(request.userOverrides())
        );

        if (outcome.needsMoreInfo()) {
            return Response.status(422)
                    .entity(toNeedsMoreInfoResponse(outcome.analysis()))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(toDetailResponse(outcome.createdRecipe()))
                .build();
    }

    private RecipeSummaryResponse toSummaryResponse(RecipeSummary recipe) {
        return new RecipeSummaryResponse(recipe.id(), recipe.legacyId(), recipe.title(), recipe.category(), recipe.excerpt());
    }

    private RecipeDetailResponse toDetailResponse(RecipeDetail recipe) {
        return new RecipeDetailResponse(
                recipe.id(),
                recipe.legacyId(),
                recipe.title(),
                recipe.category(),
                recipe.description(),
                recipe.servings(),
                recipe.ingredients(),
                recipe.preparation(),
                recipe.pdfSlug(),
                recipe.tags(),
                recipe.source()
        );
    }

    private ImportNeedsMoreInfoResponse toNeedsMoreInfoResponse(RecipeImportAnalysis analysis) {
        return new ImportNeedsMoreInfoResponse(
                "NEEDS_MORE_INFO",
                analysis.rawText(),
                toProposedPayload(analysis.proposedRecipe()),
                analysis.missingFields(),
                analysis.parseWarnings()
        );
    }

    private ProposedRecipePayload toProposedPayload(ProposedRecipe proposedRecipe) {
        return new ProposedRecipePayload(
                proposedRecipe.title(),
                proposedRecipe.description(),
                proposedRecipe.servings(),
                proposedRecipe.ingredients(),
                proposedRecipe.instructions(),
                proposedRecipe.tags(),
                proposedRecipe.source()
        );
    }

    private ProposedRecipe toProposedRecipe(ProposedRecipePayload payload) {
        return new ProposedRecipe(
                payload.title(),
                payload.description(),
                payload.servings(),
                payload.ingredients(),
                payload.instructions(),
                payload.tags(),
                payload.source()
        );
    }

    private UserRecipeOverrides toUserOverrides(UserOverridesPayload payload) {
        return new UserRecipeOverrides(
                payload.title(),
                payload.description(),
                payload.servings(),
                payload.ingredients(),
                payload.instructions(),
                payload.tags(),
                payload.source(),
                payload.notes()
        );
    }

    private boolean hasAllowedExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return false;
        }
        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return allowedExtensions.contains(extension);
    }

    private Set<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
    }
}
