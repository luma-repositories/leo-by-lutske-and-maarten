package be.lutske.leolegacy.domain;

public record IngredientConversion(String originalQuantity, String convertedQuantity,
    String unit, String ingredientName, String displayValue, boolean converted) {
}