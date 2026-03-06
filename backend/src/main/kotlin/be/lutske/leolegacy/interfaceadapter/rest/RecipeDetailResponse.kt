package be.lutske.leolegacy.interfaceadapter.rest

/**
 * JSON response DTO for a full recipe detail.
 */
data class RecipeDetailResponse(
    val id: Long,
    val title: String,
    val ingredients: List<String>,
    val preparation: String,
    val categoryId: Long,
    val categoryName: String,
    val viewCount: Int
)
