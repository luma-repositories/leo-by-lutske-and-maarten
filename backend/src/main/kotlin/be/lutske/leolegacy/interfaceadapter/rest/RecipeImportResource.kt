package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.ExtractionResult
import be.lutske.leolegacy.application.service.RecipeExtractionException
import be.lutske.leolegacy.application.service.RecipeExtractionService
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.multipart.FileUpload
import org.jboss.resteasy.reactive.RestForm
import java.time.Instant

/**
 * REST resource for importing recipes from images via LLM-based extraction.
 *
 * Flow:
 * 1. POST /import — extracts recipe data from image via LLM, always returns
 *    the extraction result for user review (never auto-saves).
 * 2. POST /import/confirm — user confirms/edits the extracted data, recipe is saved.
 */
@Path("/api/recipes/import")
class RecipeImportResource(
    private val extractionService: RecipeExtractionService,
    private val recipeRepository: RecipeRepository,
    private val categoryRepository: CategoryRepository
) {

    companion object {
        private const val MAX_FILE_SIZE = 10L * 1024 * 1024 // 10 MB
        private const val DEFAULT_IMPORT_CATEGORY_ID = 15L // "Geimporteerd"
        private val ALLOWED_CONTENT_TYPES = setOf(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
        )
        private val ALLOWED_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp")
    }

    /**
     * Upload an image and extract recipe data via LLM.
     *
     * Always returns 200 with the extraction result for user review.
     * The recipe is NOT saved at this stage — the user must review and confirm.
     *
     * Returns:
     * - 200 OK with ImportExtractionResponse (proposed recipe, missing fields, warnings)
     * - 400 Bad Request if file validation fails
     * - 502 Bad Gateway if the LLM provider is unavailable
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun importFromImage(@RestForm("file") file: FileUpload): Response {
        // Validate file
        validateFile(file)

        // Read file bytes and determine MIME type
        val imageBytes = file.uploadedFile().toFile().readBytes()
        val mimeType = file.contentType() ?: "image/jpeg"

        // Extract recipe via LLM
        val extraction: ExtractionResult = try {
            extractionService.extractRecipeFromImage(imageBytes, mimeType)
        } catch (e: RecipeExtractionException) {
            return Response.status(502)
                .entity(mapOf("error" to "AI extraction failed: ${e.message}"))
                .build()
        }

        // Always return extraction result for user review — never auto-save
        val status = if (extraction.isComplete()) "COMPLETE" else "NEEDS_MORE_INFO"

        return Response.ok(
            ImportExtractionResponse(
                status = status,
                rawModelResponse = extraction.rawModelResponse,
                proposedRecipe = extractionToProposal(extraction),
                missingFields = extraction.missingFields(),
                warnings = extraction.warnings
            )
        ).build()
    }

    /**
     * Confirm and finalize an imported recipe after user review.
     * This is the only endpoint that saves to the database.
     */
    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun confirmImport(request: ImportConfirmRequest): Response {
        val proposed = request.proposedRecipe
        val overrides = request.userOverrides

        // Merge proposed + overrides
        val title = overrides?.title ?: proposed.title
            ?: throw BadRequestException("Title is required")
        val ingredients = overrides?.ingredients ?: proposed.ingredients
            ?: throw BadRequestException("Ingredients are required")
        val preparation = overrides?.preparation ?: proposed.preparation
            ?: throw BadRequestException("Preparation is required")
        val categoryId = overrides?.categoryId ?: proposed.categoryId ?: DEFAULT_IMPORT_CATEGORY_ID
        val notes = overrides?.notes ?: proposed.notes

        if (ingredients.isEmpty()) {
            throw BadRequestException("At least one ingredient is required")
        }

        val mergedProposal = ProposedRecipeDto(
            title = title,
            ingredients = ingredients,
            preparation = if (notes.isNullOrBlank()) preparation else "$preparation\n\nNotes: $notes",
            categoryId = categoryId,
            description = overrides?.description ?: proposed.description,
            servings = overrides?.servings ?: proposed.servings,
            source = proposed.source
        )

        val recipe = createRecipeFromProposal(mergedProposal)
        return Response.status(Response.Status.CREATED)
            .entity(recipe.toDetailResponse())
            .build()
    }

    private fun validateFile(file: FileUpload) {
        val fileSize = file.uploadedFile().toFile().length()
        if (fileSize > MAX_FILE_SIZE) {
            throw BadRequestException("File too large: ${fileSize / 1024 / 1024} MB. Maximum is 10 MB.")
        }

        val contentType = file.contentType()
        if (contentType != null && contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Unsupported file type: $contentType. Allowed: PNG, JPG, JPEG, WEBP.")
        }

        val fileName = file.fileName()
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            throw BadRequestException("Unsupported file extension: .$extension. Allowed: .png, .jpg, .jpeg, .webp.")
        }
    }

    private fun extractionToProposal(extraction: ExtractionResult): ProposedRecipeDto {
        return ProposedRecipeDto(
            title = extraction.title,
            description = extraction.description,
            servings = extraction.servings,
            ingredients = extraction.ingredients,
            preparation = extraction.steps?.joinToString("\n"),
            source = extraction.source,
            tags = extraction.tags
        )
    }

    private fun createRecipeFromProposal(proposal: ProposedRecipeDto): RecipeEntity {
        val categoryId = proposal.categoryId ?: DEFAULT_IMPORT_CATEGORY_ID
        val category = categoryRepository.findById(categoryId)
            ?: categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID)
            ?: throw IllegalStateException("Default import category not found")

        val recipe = RecipeEntity()
        recipe.title = proposal.title ?: "Untitled recipe"
        recipe.ingredients = proposal.ingredients?.joinToString("|") ?: ""
        recipe.preparation = proposal.preparation ?: ""
        recipe.category = category
        recipe.viewCount = 0
        recipe.source = proposal.source ?: "Imported from image"
        recipe.createdAt = Instant.now()

        recipeRepository.persist(recipe)
        return recipe
    }

    private fun RecipeEntity.toDetailResponse() = RecipeDetailResponse(
        id = this.id,
        title = this.title,
        ingredients = this.ingredients.split("|").filter { it.isNotBlank() },
        preparation = this.preparation,
        categoryId = this.category.id,
        categoryName = this.category.name,
        viewCount = this.viewCount
    )
}
