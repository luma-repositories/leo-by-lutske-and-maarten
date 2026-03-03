package be.lutske.leolegacy.domain;

import java.util.List;

public record RecipeImportAnalysis(
        String rawText,
        ProposedRecipe proposedRecipe,
        List<String> missingFields,
        List<String> parseWarnings
) {
    public boolean needsMoreInfo() {
        return !missingFields.isEmpty() || !parseWarnings.isEmpty();
    }
}
