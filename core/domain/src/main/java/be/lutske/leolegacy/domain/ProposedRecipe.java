package be.lutske.leolegacy.domain;

import java.util.List;

public record ProposedRecipe(String title, String description, String servings,
                            List<String> ingredients, String preparation, String source,
                            List<String> tags, Long categoryId, String notes,
                            List<IngredientConversion> convertedIngredients) {
}