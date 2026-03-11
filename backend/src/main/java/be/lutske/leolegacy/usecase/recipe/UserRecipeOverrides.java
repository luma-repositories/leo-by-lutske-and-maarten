package be.lutske.leolegacy.usecase.recipe;

import java.util.List;

public record UserRecipeOverrides(
        String title,
        List<String> ingredients,
        String preparation,
        Long categoryId,
        String notes,
        String servings,
        String description
) {
    public UserRecipeOverrides {
        ingredients = ingredients == null ? null : List.copyOf(ingredients);
    }
}
