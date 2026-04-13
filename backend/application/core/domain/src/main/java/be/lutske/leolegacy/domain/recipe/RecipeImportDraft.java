package be.lutske.leolegacy.domain.recipe;

import java.time.Instant;
import java.util.List;

public record RecipeImportDraft(
        String title,
        List<String> ingredients,
        String preparation,
        long categoryId,
        String source,
        Instant createdAt,
        String importMetadata
) {
    public RecipeImportDraft {
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
    }
}
