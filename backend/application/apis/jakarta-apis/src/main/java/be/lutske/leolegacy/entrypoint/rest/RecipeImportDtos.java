package be.lutske.leolegacy.entrypoint.rest;

import java.util.List;

public final class RecipeImportDtos {

    private RecipeImportDtos() {
    }

    public record ProposedRecipeDto(
            String title,
            String description,
            String servings,
            List<String> ingredients,
            String preparation,
            String source,
            List<String> tags,
            Long categoryId,
            String notes
    ) {
    }

    public record ImportExtractionResponse(
            String status,
            String rawModelResponse,
            ProposedRecipeDto proposedRecipe,
            List<String> missingFields,
            List<String> warnings
    ) {
    }

    public record ImportConfirmRequest(
            String rawModelResponse,
            ProposedRecipeDto proposedRecipe,
            UserOverrides userOverrides
    ) {
    }

    public record UserOverrides(
            String title,
            List<String> ingredients,
            String preparation,
            Long categoryId,
            String notes,
            String servings,
            String description
    ) {
    }
}
