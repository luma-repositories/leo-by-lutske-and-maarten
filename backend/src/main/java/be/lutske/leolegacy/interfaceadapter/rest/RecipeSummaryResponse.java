package be.lutske.leolegacy.interfaceadapter.rest;

/**
 * JSON response DTO for a recipe summary (used in list views).
 */
public record RecipeSummaryResponse(
        long id,
        String title,
        String categoryName,
        int viewCount
) {
}
