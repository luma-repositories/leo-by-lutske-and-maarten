package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.service.IngredientConversion;
import java.util.List;

/**
 * DTOs for the recipe import flow.
 * Field names must match the frontend TypeScript interfaces in api/client.ts.
 */
public final class RecipeImportDtos {

    private RecipeImportDtos() {
    }

    /**
     * Proposed recipe extracted by the AI model.
     * Uses {@code preparation} (joined string) to match the frontend contract.
     */
    public record ProposedRecipeDto(
            String title,
            String description,
            String servings,
            List<String> ingredients,
            String preparation,
            String source,
            List<String> tags,
            Long categoryId,
            String notes,
            List<IngredientConversion> convertedIngredients
    ) {}

    /**
     * Unified response from {@code POST /api/recipes/import}.
     * Always HTTP 200. Status is "COMPLETE" or "NEEDS_MORE_INFO".
     */
    public record ImportExtractionResponse(
            String status,
            String rawModelResponse,
            ProposedRecipeDto proposedRecipe,
            List<String> missingFields,
            List<String> warnings
    ) {}

    /**
     * Request body for confirming/finalizing an imported recipe.
     * Matches the frontend {@code ImportConfirmRequest} interface.
     */
    public record ImportConfirmRequest(
            String rawModelResponse,
            ProposedRecipeDto proposedRecipe,
            UserOverrides userOverrides
    ) {}

    /**
     * User-provided overrides and corrections for the imported recipe.
     */
    public record UserOverrides(
            String title,
            List<String> ingredients,
            String preparation,
            Long categoryId,
            String notes,
            String servings,
            String description
    ) {}
}
