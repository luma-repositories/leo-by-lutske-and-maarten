package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeExtraction;
import be.lutske.leolegacy.usecase.recipe.ConfirmRecipeImportCommand;
import be.lutske.leolegacy.usecase.recipe.ImportedRecipeProposal;
import be.lutske.leolegacy.usecase.recipe.UserRecipeOverrides;

final class RestRecipeMapper {

    private RestRecipeMapper() {
    }

    static RecipeSummaryResponse toSummaryResponse(Recipe recipe) {
        return new RecipeSummaryResponse(
                recipe.id(),
                recipe.title(),
                recipe.category().name(),
                recipe.viewCount()
        );
    }

    static RecipeDetailResponse toDetailResponse(Recipe recipe) {
        return new RecipeDetailResponse(
                recipe.id(),
                recipe.title(),
                recipe.ingredients(),
                recipe.preparation(),
                recipe.category().id(),
                recipe.category().name(),
                recipe.viewCount()
        );
    }

    static RecipeImportDtos.ProposedRecipeDto toProposedRecipeDto(RecipeExtraction extraction) {
        String preparation = extraction.steps() != null ? String.join("\n", extraction.steps()) : null;
        return new RecipeImportDtos.ProposedRecipeDto(
                extraction.title(),
                extraction.description(),
                extraction.servings(),
                extraction.ingredients(),
                preparation,
                extraction.source(),
                extraction.tags(),
                null,
                null
        );
    }

    static ConfirmRecipeImportCommand toConfirmCommand(RecipeImportDtos.ImportConfirmRequest request) {
        var proposedRecipe = request.proposedRecipe();
        var userOverrides = request.userOverrides();
        return new ConfirmRecipeImportCommand(
                request.rawModelResponse(),
                new ImportedRecipeProposal(
                        proposedRecipe.title(),
                        proposedRecipe.description(),
                        proposedRecipe.servings(),
                        proposedRecipe.ingredients(),
                        proposedRecipe.preparation(),
                        proposedRecipe.source(),
                        proposedRecipe.tags(),
                        proposedRecipe.categoryId(),
                        proposedRecipe.notes()
                ),
                userOverrides == null ? null : new UserRecipeOverrides(
                        userOverrides.title(),
                        userOverrides.ingredients(),
                        userOverrides.preparation(),
                        userOverrides.categoryId(),
                        userOverrides.notes(),
                        userOverrides.servings(),
                        userOverrides.description()
                )
        );
    }
}
