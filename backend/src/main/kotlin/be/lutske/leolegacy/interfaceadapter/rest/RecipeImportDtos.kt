package be.lutske.leolegacy.interfaceadapter.rest

/**
 * Partial recipe DTO used during the import flow.
 * Fields are nullable because the LLM may not extract everything.
 */
data class ProposedRecipeDto(
    val title: String? = null,
    val description: String? = null,
    val servings: String? = null,
    val ingredients: List<String>? = null,
    val preparation: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null,
    val source: String? = null,
    val tags: List<String>? = null
)

/**
 * Response returned when LLM extraction succeeds but the parsed recipe is incomplete.
 * HTTP 422 Unprocessable Entity.
 */
data class ImportNeedsMoreInfoResponse(
    val status: String = "NEEDS_MORE_INFO",
    val rawModelResponse: String? = null,
    val proposedRecipe: ProposedRecipeDto,
    val missingFields: List<String>,
    val warnings: List<String>
)

/**
 * Request body for confirming/finalizing an imported recipe.
 */
data class ImportConfirmRequest(
    val rawModelResponse: String? = null,
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
    val notes: String? = null,
    val servings: String? = null,
    val description: String? = null
)
