package be.lutske.leolegacy.interfaceadapter.rest;

public record RecipeImportConfirmRequest(
        String rawText,
        ProposedRecipePayload proposedRecipe,
        UserOverridesPayload userOverrides
) {
}
