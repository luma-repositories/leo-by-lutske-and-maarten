package be.lutske.leolegacy.domain;

import java.time.Instant;
import java.util.List;

public record NewRecipe(String title, List<String> ingredients, String preparation,
                       long categoryId, int viewCount, String source, Instant createdAt,
                       String importMetadata) {
    public NewRecipe {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("NewRecipe title cannot be null or blank");
        }
        if (ingredients == null) {
            throw new IllegalArgumentException("NewRecipe ingredients cannot be null");
        }
        if (categoryId < 0) {
            throw new IllegalArgumentException("NewRecipe categoryId cannot be negative");
        }
        if (viewCount < 0) {
            throw new IllegalArgumentException("NewRecipe viewCount cannot be negative");
        }
        if (ingredients.contains(null)) {
            throw new IllegalArgumentException("NewRecipe ingredients cannot contain null entries");
        }
    }
}