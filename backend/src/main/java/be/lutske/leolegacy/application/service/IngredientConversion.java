package be.lutske.leolegacy.application.service;

/**
 * Structured representation of an ingredient line with metric → English unit conversion.
 */
public record IngredientConversion(
        String originalQuantity,
        String convertedQuantity,
        String unit,
        String ingredientName,
        String displayValue,
        boolean converted
) {
}

