package be.lutske.leolegacy.application.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class IngredientConverter {

    private static final double GRAM_TO_OUNCE = 0.035274;
    private static final double KILOGRAM_TO_POUND = 2.20462;
    private static final double MILLILITER_TO_CUP = 0.00422675;
    private static final double LITER_TO_CUP = 4.22675;

    private static final Pattern METRIC_PATTERN = Pattern.compile(
            "^\\s*(\\d+(?:[.,]\\d+)?)(?:\\s*)(kg|kilogrammi?|kilograms?|g|gr|grammi?|grams?|ml|millilit(e)?rs?|millilitri?|l|liters?|litri|liter)\\b\\s*(.*)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IMPERIAL_PATTERN = Pattern.compile(
            "\\b(oz|ounce|ounces|lb|lbs|pound|pounds|cup|cups|quart|quarts|qt|fl\\.?\\s?oz|fluid\\s?ounce|pint|pints)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VAGUE_PATTERN = Pattern.compile(
            "\\b(qb|q\\.b\\.|quanto basta|to taste|taste|bunch|handful)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public IngredientConversion convert(String ingredientLine) {
        if (ingredientLine == null || ingredientLine.isBlank()) {
            return new IngredientConversion(null, null, null, null, "", false);
        }
        String trimmed = ingredientLine.trim();

        if (VAGUE_PATTERN.matcher(trimmed).find() || IMPERIAL_PATTERN.matcher(trimmed).find()) {
            return new IngredientConversion(null, null, null, trimmed, trimmed, false);
        }

        var matcher = METRIC_PATTERN.matcher(trimmed);
        if (!matcher.find()) {
            return new IngredientConversion(null, null, null, trimmed, trimmed, false);
        }

        String valueRaw = matcher.group(1).trim();
        String unitRaw = matcher.group(2).trim();
        String ingredientName = matcher.group(4) != null ? matcher.group(4).trim() : "";

        Unit unit = Unit.from(unitRaw);
        if (unit == null) {
            return new IngredientConversion(null, null, null, trimmed, trimmed, false);
        }

        double numericValue;
        try {
            numericValue = Double.parseDouble(valueRaw.replace(',', '.'));
        } catch (NumberFormatException e) {
            return new IngredientConversion(null, null, null, trimmed, trimmed, false);
        }

        ConversionResult conversion = convertAmount(numericValue, unit);
        if (conversion == null) {
            return new IngredientConversion(null, null, null, trimmed, trimmed, false);
        }

        String originalQuantity = valueRaw + " " + unit.metricLabel();
        String convertedQuantity = conversion.value() + " " + conversion.unitLabel();
        String display = buildDisplay(convertedQuantity, originalQuantity, ingredientName);

        return new IngredientConversion(
                originalQuantity,
                convertedQuantity,
                conversion.unitLabel(),
                ingredientName.isBlank() ? null : ingredientName,
                display,
                true
        );
    }

    public List<IngredientConversion> convertAll(List<String> ingredients) {
        if (ingredients == null) {
            return Collections.emptyList();
        }
        return ingredients.stream()
                .map(this::convert)
                .toList();
    }

    public String convertToEnglishUnits(String ingredientLine) {
        return convert(ingredientLine).displayValue();
    }

    private ConversionResult convertAmount(double value, Unit unit) {
        return switch (unit) {
            case GRAM -> new ConversionResult(format(value * GRAM_TO_OUNCE, 1), "oz");
            case KILOGRAM -> new ConversionResult(format(value * KILOGRAM_TO_POUND, 1), "lb");
            case MILLILITER -> new ConversionResult(formatCups(value * MILLILITER_TO_CUP), "cups");
            case LITER -> new ConversionResult(formatCups(value * LITER_TO_CUP), "cups");
        };
    }

    private String buildDisplay(String converted, String original, String ingredientName) {
        StringBuilder sb = new StringBuilder();
        sb.append(converted).append(" (").append(original).append(")");
        if (ingredientName != null && !ingredientName.isBlank()) {
            sb.append(" ").append(ingredientName);
        }
        return sb.toString().trim();
    }

    private String format(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private String formatCups(double cups) {
        int scale = cups < 1 ? 2 : 1;
        return format(cups, scale);
    }

    private enum Unit {
        GRAM("g"),
        KILOGRAM("kg"),
        MILLILITER("ml"),
        LITER("liter");

        private final String metricLabel;

        Unit(String metricLabel) {
            this.metricLabel = metricLabel;
        }

        String metricLabel() {
            return metricLabel;
        }

        static Unit from(String raw) {
            String normalized = raw.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "g", "gram", "grams", "gr", "grammi" -> GRAM;
                case "kg", "kilogram", "kilograms", "kilogrammi" -> KILOGRAM;
                case "ml", "milliliter", "milliliters", "millilitre", "millilitres", "millilitr", "millilitri" -> MILLILITER;
                case "l", "liter", "liters", "litre", "litres", "litri" -> LITER;
                default -> null;
            };
        }
    }

    private record ConversionResult(String value, String unitLabel) {}
}

