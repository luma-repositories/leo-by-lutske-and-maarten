package be.lutske.leolegacy.domain;

public record UserRecipeOverrides(
        String title,
        String description,
        Integer servings,
        String ingredients,
        String instructions,
        String tags,
        String source,
        String notes
) {
}
