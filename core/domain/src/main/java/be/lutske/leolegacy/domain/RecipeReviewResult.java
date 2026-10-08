package be.lutske.leolegacy.domain;

import java.util.List;

public record RecipeReviewResult(String status, String rawModelResponse,
                               ProposedRecipe proposedRecipe, List<String> missingFields,
                               List<String> warnings) {
}