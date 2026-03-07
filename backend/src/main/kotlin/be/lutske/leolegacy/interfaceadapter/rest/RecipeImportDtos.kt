package be.lutske.leolegacy.interfaceadapter.rest

/**
 * Partial recipe DTO used during the import flow.
 * Fields are nullable because OCR may not extract everything.
 */
data class ProposedRecipeDto(
    val title: String? = null,
    val ingredients: List<String>? = null,
    val preparation: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null
)

/**
 * Response returned when OCR succeeds but the parsed recipe is incomplete.
 * HTTP 422 Unprocessable Entity.
 */
data class ImportNeedsMoreInfoResponse(
    val status: String = "NEEDS_MORE_INFO",
    val rawText: String,
    val proposedRecipe: ProposedRecipeDto,
    val missingFields: List<String>,
    val parseWarnings: List<String>
)

/**
 * Request body for confirming/finalizing an imported recipe.
 */
data class ImportConfirmRequest(
    val rawText: String,
    val proposedRecipe: ProposedRecipeDto,
    val userOverrides: UserOverrides? = null
)

/**
 * User-provided overrides and corrections for the imported recipe.
 */
data class UserOverrides(
    val title: String? = null,
    val ingredients: List<String>? = null,
    val preparation: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null
)
