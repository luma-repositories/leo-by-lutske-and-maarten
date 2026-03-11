package be.lutske.leolegacy.entrypoint.rest.dto;

import java.util.List;

public record RecipeDetailResponse(
        long id,
        String title,
        List<String> ingredients,
        String preparation,
        long categoryId,
        String categoryName,
        int viewCount
) {}
