package be.lutske.leolegacy.interfaceadapter.rest

/**
 * JSON response DTO for a recipe category.
 */
data class CategoryResponse(
    val id: Long,
    val name: String,
    val recipeCount: Long
)
