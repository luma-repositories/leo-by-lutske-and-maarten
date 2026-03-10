package be.lutske.leolegacy.interfaceadapter.rest;

import java.util.List;

/**
 * DTOs for the recipe import flow.
 */
public final class RecipeImportDtos {

    private RecipeImportDtos() {
    }

    /**
     * Partial recipe DTO used during the import flow.
     * Fields are nullable because OCR may not extract everything.
     */
    public record ProposedRecipeDto(
            String title,
            List<String> ingredients,
            String preparation,
            Long categoryId,
            String notes
    ) {}

    /**
     * Response returned when OCR succeeds but the parsed recipe is incomplete.
     * HTTP 422 Unprocessable Entity.
     */
    public record ImportNeedsMoreInfoResponse(
            String status,
            String rawText,
            ProposedRecipeDto proposedRecipe,
            List<String> missingFields,
            List<String> parseWarnings
    ) {
        public ImportNeedsMoreInfoResponse(String rawText, ProposedRecipeDto proposedRecipe,
                                           List<String> missingFields, List<String> parseWarnings) {
            this("NEEDS_MORE_INFO", rawText, proposedRecipe, missingFields, parseWarnings);
        }
    }

    /**
     * Request body for confirming/finalizing an imported recipe.
     */
    public record ImportConfirmRequest(
            String rawText,
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
            String notes
    ) {}
}
