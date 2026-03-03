package be.lutske.leolegacy.interfaceadapter.rest;

public record ProposedRecipePayload(
        String title,
        String description,
        Integer servings,
        String ingredients,
        String instructions,
        String tags,
        String source
) {
}
