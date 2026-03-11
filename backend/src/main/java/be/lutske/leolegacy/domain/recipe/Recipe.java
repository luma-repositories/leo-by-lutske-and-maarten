package be.lutske.leolegacy.domain.recipe;

import be.lutske.leolegacy.domain.category.Category;

import java.time.Instant;
import java.util.List;

public record Recipe(
        long id,
        String title,
        List<String> ingredients,
        String preparation,
        Category category,
        int viewCount,
        String source,
        Instant createdAt,
        String importMetadata
) {
    public Recipe {
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
    }
}
