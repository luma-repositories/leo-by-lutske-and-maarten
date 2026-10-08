package be.lutske.leolegacy.domain;

import java.util.List;

public record RecipeDetail(long id, String title, List<String> ingredients,
                          String preparation, long categoryId, String categoryName,
                          int viewCount) {
    public RecipeDetail {
        if (title == null) {
            throw new IllegalArgumentException("RecipeDetail title cannot be null");
        }
        if (categoryId < 0) {
            throw new IllegalArgumentException("RecipeDetail categoryId cannot be negative");
        }
        if (categoryName == null) {
            throw new IllegalArgumentException("RecipeDetail categoryName cannot be null");
        }
    }
}