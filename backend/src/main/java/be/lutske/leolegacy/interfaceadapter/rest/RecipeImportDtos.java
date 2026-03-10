package be.lutske.leolegacy.interfaceadapter.rest;

import java.util.List;

/**
 * DTOs for the recipe import flow.
 */
public final class RecipeImportDtos {

    private RecipeImportDtos() {
    }

    /**
     * Proposed recipe extracted by the AI model.
     * Fields are nullable because the model may not extract everything.
     */
    public record ProposedRecipeDto(
            String title,
            String description,
            String servings,
            List<String> ingredients,
            List<String> steps,
            String source,
            List<String> tags,
            Long categoryId,
            String notes
    ) {}

    /**
     * Response returned when AI extraction succeeds but the parsed recipe is incomplete.
     * HTTP 422 Unprocessable Entity.
     */
    public record ImportNeedsMoreInfoResponse(
            String status,
            String rawModelResponse,
            ProposedRecipeDto proposedRecipe,
            List<String> missingFields,
            List<String> warnings,
            String provider,
            String model
    ) {
        public ImportNeedsMoreInfoResponse(String rawModelResponse, ProposedRecipeDto proposedRecipe,
                                           List<String> missingFields, List<String> warnings,
                                           String provider, String model) {
            this("NEEDS_MORE_INFO", rawModelResponse, proposedRecipe, missingFields, warnings, provider, model);
        }
    }

    /**
     * Request body for confirming/finalizing an imported recipe.
     */
    public record ImportConfirmRequest(
            ProposedRecipeDto proposedRecipe,
            UserOverrides userOverrides
    ) {}

    /**
     * User-provided overrides and corrections for the imported recipe.
     */
    public record UserOverrides(
            String title,
            List<String> ingredients,
            List<String> steps,
            Long categoryId,
            String notes
    ) {}
}
