package be.lutske.leolegacy.interfaceadapter.rest;

import java.util.List;

/**
 * JSON response DTO for a full recipe detail.
 */
public record RecipeDetailResponse(
        long id,
        String title,
        List<String> ingredients,
        String preparation,
        long categoryId,
        String categoryName,
        int viewCount
) {
}
