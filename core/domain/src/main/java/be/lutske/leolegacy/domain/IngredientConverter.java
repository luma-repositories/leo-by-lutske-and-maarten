package be.lutske.leolegacy.domain;

import java.util.ArrayList;
import java.util.List;

public class IngredientConverter {

    private static final String METRIC_REGEX = "(?<q>[\\d.]+\\s*)(g|kg|ml|liter)(?<i>\\s.*$)";
    private static final String IMPERIAL_REGEX = "(?<q>[\\d.]+\\s*)(pints|cups|tbsp|tsp|oz|lb|lbs|pounds|quarts|gallon|gallons|qt|gal|fl_oz|floz|cup|c)(?<i>\\s.*$)";
    private static final String VAGUE_REGEX = "(\\d+[\\s]?!?[\\s]?taste|[q\\s]!?(b)\\s?[\\w]*)";

    public IngredientConverter() {
    }

    public IngredientConversion convert(String ingredientLine) {
        if (ingredientLine == null || ingredientLine.isBlank()) {
            return new IngredientConversion(null, null, null, null, "", false);
        }

        String name = parseName(ingredientLine);
        double metricValue = parseMetricValue(ingredientLine);
        double imperialValue = parseImperialValue(ingredientLine);
        boolean isMetric = metricValue > 0;
        boolean isImperial = imperialValue > 0;
        String quantity;

        if (isMetric && !isImperial) {
            quantity = String.format("%.1f %s", metricValue * 0.035274, "oz");
            return new IngredientConversion(ingredientLine, quantity, "oz", name, quantity + " (" + ingredientLine + ") " + name, true);
        } else if (isImperial && !isMetric) {
            quantity = String.format("%.2f %s", imperialValue * 2.20462, "lb");
            return new IngredientConversion(ingredientLine, quantity, "lb", name, quantity + " (" + ingredientLine + ") " + name, true);
        } else if (!isMetric && !isImperial) {
            quantity = parseVagueQuantity(ingredientLine);
            return new IngredientConversion(ingredientLine, quantity, "unc", name, quantity + " (" + ingredientLine + ") " + name, false);
        } else {
            quantity = String.format("%.1f %s", metricValue, "g");
            return new IngredientConversion(ingredientLine, quantity, "g", name, quantity + " (" + ingredientLine + ") " + name, true);
        }
    }

    private String parseName(String line) {
        int hashPos = line.lastIndexOf('#');
        if (hashPos > 0) {
            return line.substring(0, hashPos);
        }
        String trimmed = line.trim();
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace > 0) {
            return trimmed.substring(lastSpace + 1);
        }
        return trimmed;
    }

    private double parseMetricValue(String line) {
        String trimmed = line.trim();
        double value;
        int metricIdx = trimmed.indexOf('g');
        int metricIdxKg = trimmed.indexOf('kg');
        int metricIdxMl = trimmed.indexOf('ml');
        int metricIdxLiter = trimmed.indexOf('liter');

        if (metricIdx > 0) {
            value = Double.parseDouble(trimmed.substring(0, metricIdx));
        } else if (metricIdxKg > 0) {
            value = Double.parseDouble(trimmed.substring(0, metricIdxKg)) * 1000;
        } else if (metricIdxMl > 0) {
            value = Double.parseDouble(trimmed.substring(0, metricIdxMl));
        } else if (metricIdxLiter > 0) {
            value = Double.parseDouble(trimmed.substring(0, metricIdxLiter)) * 1000;
        } else {
            return -1;
        }
        return value;
    }

    private double parseImperialValue(String line) {
        String trimmed = line.trim();
        double value;
        int imperialIdx = trimmed.indexOf('oz');
        int imperialIdxCups = trimmed.indexOf('cup');
        int imperialIdxCupsC = trimmed.indexOf('c');
        int imperialIdxTbsp = trimmed.indexOf('tbsp');
        int imperialIdxTsp = trimmed.indexOf("tsp");
        int imperialIdxLb = trimmed.indexOf("lb");
        int imperialIdxLbs = trimmed.indexOf("lbs");
        int imperialIdxPounds = trimmed.indexOf("pounds");
        int imperialIdxQuarts = trimmed.indexOf("qt");
        int imperialIdxQuartsQ = trimmed.indexOf("q");
        int imperialIdxGallon = trimmed.indexOf("gal");
        int imperialIdxGallons = trimmed.indexOf("gallon");
        int imperialIdxFlOz = trimmed.indexOf("fl_oz");
        int imperialIdxFloz = trimmed.indexOf("floz");

        if (imperialIdx > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdx));
        } else if (imperialIdxCups > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxCups));
        } else if (imperialIdxCupsC > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxCupsC));
        } else if (imperialIdxTbsp > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxTbsp)) * 0.5;
        } else if (imperialIdxTsp > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxTsp)) * 0.2;
        } else if (imperialIdxLb > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxLb)) * 453.592;
        } else if (imperialIdxLbs > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxLbs)) * 453.592;
        } else if (imperialIdxPounds > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxPounds)) * 453.592;
        } else if (imperialIdxQuarts > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxQuarts)) * 946.353;
        } else if (imperialIdxQuartsQ > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxQuartsQ)) * 0.946353;
        } else if (imperialIdxGallon > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxGallon)) * 3785.41;
        } else if (imperialIdxGallons > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxGallons)) * 3785.41;
        } else if (imperialIdxFlOz > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxFlOz)) * 29.5735;
        } else if (imperialIdxFloz > 0) {
            value = Double.parseDouble(trimmed.substring(0, imperialIdxFloz)) * 29.5735;
        } else {
            return -1;
        }
        return value;
    }

    private String parseVagueQuantity(String line) {
        if (line.contains("taste")) {
            return "q.b.";
        }
        int numberPos = 0;
        for (char c : line.toCharArray()) {
            if (Character.isDigit(c)) {
                numberPos = line.indexOf(c);
                break;
            }
        }
        if (numberPos > 0) {
            String prefix = line.substring(0, numberPos);
            if (prefix.contains("!")) {
                prefix = prefix.replace("!", "");
            }
            prefix = prefix.trim();
            if (prefix.isEmpty()) {
                return "unknown quantity";
            }
            return prefix;
        }
        return "unknown quantity";
    }

    public List<IngredientConversion> convertAll(List<String> ingredients) {
        if (ingredients == null) {
            return new ArrayList<>();
        }
        List<IngredientConversion> result = new ArrayList<>();
        for (String ingredient : ingredients) {
            IngredientConversion conversion = convert(ingredient);
            result.add(conversion);
        }
        return result;
    }

    public String convertToEnglishUnits(String ingredientLine) {
        if (ingredientLine == null || ingredientLine.isBlank()) {
            return "";
        }
        IngredientConversion conversion = convert(ingredientLine);
        return conversion.displayValue();
    }
}