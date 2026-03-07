package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.OcrException
import be.lutske.leolegacy.application.service.OcrService
import be.lutske.leolegacy.application.service.RecipeParserService
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
 * REST resource for importing recipes from images via OCR.
 */
@Path("/api/recipes/import")
class RecipeImportResource(
    private val ocrService: OcrService,
    private val parserService: RecipeParserService,
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
     * Upload an image and attempt to extract a recipe via OCR.
     *
     * Returns:
     * - 201 Created with RecipeDetailResponse if recipe was fully parsed and saved
     * - 422 Unprocessable Entity with ImportNeedsMoreInfoResponse if parsing is incomplete
     * - 400 Bad Request if file validation fails
     * - 500 Internal Server Error if OCR fails
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun importFromImage(@RestForm("file") file: FileUpload): Response {
        // Validate file
        validateFile(file)

        // Read file bytes
        val imageBytes = file.uploadedFile().toFile().readBytes()

        // Perform OCR
        val rawText: String = try {
            ocrService.extractText(imageBytes)
        } catch (e: OcrException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "OCR failed: ${e.message}"))
                .build()
        }

        // Parse the OCR text
        val parseResult = parserService.parse(rawText)

        // If all required fields are present, create the recipe directly
        if (parseResult.missingFields.isEmpty()) {
            val recipe = createRecipeFromProposal(parseResult.proposedRecipe)
            return Response.status(Response.Status.CREATED)
                .entity(recipe.toDetailResponse())
                .build()
        }

        // Otherwise, return needs-more-info
        return Response.status(422)
            .entity(
                ImportNeedsMoreInfoResponse(
                    rawText = rawText,
                    proposedRecipe = parseResult.proposedRecipe,
                    missingFields = parseResult.missingFields,
                    parseWarnings = parseResult.parseWarnings
                )
            )
            .build()
    }

    /**
     * Confirm and finalize an imported recipe after user provides missing info.
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
            preparation = if (notes.isNullOrBlank()) preparation else "$preparation\n\nNotities: $notes",
            categoryId = categoryId
        )

        val recipe = createRecipeFromProposal(mergedProposal)
        return Response.status(Response.Status.CREATED)
            .entity(recipe.toDetailResponse())
            .build()
    }

    private fun validateFile(file: FileUpload) {
        // Check file size
        val fileSize = file.uploadedFile().toFile().length()
        if (fileSize > MAX_FILE_SIZE) {
            throw BadRequestException("File too large: ${fileSize / 1024 / 1024} MB. Maximum is 10 MB.")
        }

        // Check content type
        val contentType = file.contentType()
        if (contentType != null && contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Unsupported file type: $contentType. Allowed: PNG, JPG, JPEG, WEBP.")
        }

        // Check file extension
        val fileName = file.fileName()
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            throw BadRequestException("Unsupported file extension: .$extension. Allowed: .png, .jpg, .jpeg, .webp.")
        }
    }

    private fun createRecipeFromProposal(proposal: ProposedRecipeDto): RecipeEntity {
        val categoryId = proposal.categoryId ?: DEFAULT_IMPORT_CATEGORY_ID
        val category = categoryRepository.findById(categoryId)
            ?: categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID)
            ?: throw IllegalStateException("Default import category not found")

        val recipe = RecipeEntity()
        recipe.title = proposal.title ?: "Naamloos recept"
        recipe.ingredients = proposal.ingredients?.joinToString("|") ?: ""
        recipe.preparation = proposal.preparation ?: ""
        recipe.category = category
        recipe.viewCount = 0
        recipe.source = "Imported from image"
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
