package be.lutske.leolegacy.interfaceadapter.rest

/**
 * JSON response DTO for a recipe summary (used in list views).
 */
data class RecipeSummaryResponse(
    val id: Long,
    val title: String,
    val categoryName: String,
    val viewCount: Int
)
