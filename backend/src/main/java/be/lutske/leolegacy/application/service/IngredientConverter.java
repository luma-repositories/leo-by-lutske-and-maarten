package be.lutske.leolegacy.application.service;

import be.lutske.leolegacy.domain.IngredientConversion;

public class IngredientConverter {

    private final be.lutske.leolegacy.domain.IngredientConverter delegate = new be.lutske.leolegacy.domain.IngredientConverter();

    public IngredientConverter() {
    }

    public String convert(String ingredientLine) {
        return delegate.convert(ingredientLine).displayValue();
    }

    public String convertToEnglishUnits(String ingredientLine) {
        return delegate.convertToEnglishUnits(ingredientLine);
    }

    public String convertAll(List<String> ingredients) {
        return delegate.convertAll(ingredients).stream()
            .map(this::convertLegacyFormat)
            .toList();
    }

    private String convertLegacyFormat(IngredientConversion conversion) {
        String originalQuantity = conversion.originalQuantity();
        String convertedQuantity = conversion.convertedQuantity();
        String unit = conversion.unit();
        String ingredientName = conversion.ingredientName();
        String displayValue = conversion.displayValue();
        boolean converted = conversion.converted();

        if (originalQuantity == null && converted) {
            originalQuantity = convertedQuantity;
        }

        // Return in legacy format: unit, quantity, name
        return unit + " " + originalQuantity + " " + ingredientName;
    }
}