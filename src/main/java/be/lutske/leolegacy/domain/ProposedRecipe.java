package be.lutske.leolegacy.domain;

public record ProposedRecipe(
        String title,
        String description,
        Integer servings,
        String ingredients,
        String instructions,
        String tags,
        String source
) {
}
