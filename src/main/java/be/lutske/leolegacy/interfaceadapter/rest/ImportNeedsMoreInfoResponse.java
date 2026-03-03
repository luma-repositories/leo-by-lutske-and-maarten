package be.lutske.leolegacy.interfaceadapter.rest;

import java.util.List;

public record ImportNeedsMoreInfoResponse(
        String status,
        String rawText,
        ProposedRecipePayload proposedRecipe,
        List<String> missingFields,
        List<String> parseWarnings
) {
}
