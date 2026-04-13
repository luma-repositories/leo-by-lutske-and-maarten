package be.lutske.leolegacy.usecase.recipe;

public record ConfirmRecipeImportCommand(
        String rawModelResponse,
        ImportedRecipeProposal proposedRecipe,
        UserRecipeOverrides userOverrides
) {
}
