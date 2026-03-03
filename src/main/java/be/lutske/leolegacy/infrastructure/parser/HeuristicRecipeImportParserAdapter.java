package be.lutske.leolegacy.infrastructure.parser;

import be.lutske.leolegacy.application.port.RecipeImportParserPort;
import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.RecipeImportAnalysis;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class HeuristicRecipeImportParserAdapter implements RecipeImportParserPort {

    private static final Pattern INGREDIENT_LINE_PATTERN = Pattern.compile(
            "^(?:\\d+[\\d/.,-]*|\\d/\\d|[¼½¾])\\s*(?:g|kg|ml|l|tl|el|tbsp|tsp|cup|cups|oz|lb|cl|gr|gram|grams|teaspoon|tablespoon)?\\b.*",
            Pattern.CASE_INSENSITIVE
    );

    private final String defaultSource;

    public HeuristicRecipeImportParserAdapter(@ConfigProperty(name = "app.import.default-source", defaultValue = "Imported from image") String defaultSource) {
        this.defaultSource = defaultSource;
    }

    @Override
    public RecipeImportAnalysis parse(String rawText) {
        String normalizedText = rawText == null ? "" : rawText.replace("\r", "").trim();
        List<String> warnings = new ArrayList<>();
        List<String> missingFields = new ArrayList<>();

        if (normalizedText.isBlank()) {
            warnings.add("OCR did not extract readable text.");
            missingFields.add("title");
            missingFields.add("ingredients");
            missingFields.add("instructions");
            return new RecipeImportAnalysis("", new ProposedRecipe(null, null, null, null, null, null, defaultSource), missingFields, warnings);
        }

        List<String> lines = splitNonEmptyLines(normalizedText);
        String title = detectTitle(lines);

        int ingredientsIndex = findHeadingIndex(lines, "ingredient", "ingredients");
        int instructionsIndex = findHeadingIndex(lines, "instruction", "instructions", "direction", "directions", "method", "steps", "bereiding");

        String ingredients = null;
        String instructions = null;

        if (ingredientsIndex >= 0) {
            int start = ingredientsIndex + 1;
            int end = instructionsIndex > ingredientsIndex ? instructionsIndex : lines.size();
            ingredients = joinLines(lines.subList(start, end));
        }

        if (instructionsIndex >= 0) {
            instructions = joinLines(lines.subList(instructionsIndex + 1, lines.size()));
        }

        if (ingredients == null || ingredients.isBlank() || instructions == null || instructions.isBlank()) {
            HeuristicSection section = parseWithoutHeadings(lines, title);
            if ((ingredients == null || ingredients.isBlank()) && section.ingredients() != null) {
                ingredients = section.ingredients();
            }
            if ((instructions == null || instructions.isBlank()) && section.instructions() != null) {
                instructions = section.instructions();
            }
            warnings.addAll(section.warnings());
        }

        if (title == null || title.isBlank()) {
            missingFields.add("title");
        }
        if (ingredients == null || ingredients.isBlank()) {
            missingFields.add("ingredients");
        }
        if (instructions == null || instructions.isBlank()) {
            missingFields.add("instructions");
        }

        ProposedRecipe proposedRecipe = new ProposedRecipe(
                emptyToNull(title),
                null,
                null,
                emptyToNull(ingredients),
                emptyToNull(instructions),
                null,
                defaultSource
        );

        return new RecipeImportAnalysis(normalizedText, proposedRecipe, missingFields, warnings);
    }

    private String detectTitle(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        for (String line : lines) {
            if (isHeading(line)) {
                break;
            }
            if (!line.isBlank()) {
                return line;
            }
        }

        for (String line : lines) {
            if (line.equals(line.toUpperCase(Locale.ROOT)) && line.length() > 4) {
                return line;
            }
        }

        return lines.get(0);
    }

    private HeuristicSection parseWithoutHeadings(List<String> lines, String title) {
        List<String> warnings = new ArrayList<>();
        List<String> ingredientLines = new ArrayList<>();
        List<String> instructionLines = new ArrayList<>();

        for (String line : lines) {
            if (title != null && line.equals(title)) {
                continue;
            }
            if (looksLikeIngredientLine(line)) {
                ingredientLines.add(line);
            } else {
                instructionLines.add(line);
            }
        }

        if (ingredientLines.isEmpty()) {
            warnings.add("Could not confidently identify ingredient lines.");
        }
        if (instructionLines.isEmpty()) {
            warnings.add("Could not confidently identify instruction lines.");
        }
        if (!warnings.isEmpty()) {
            warnings.add("Recipe structure is ambiguous; please review extracted text.");
        }

        return new HeuristicSection(joinLines(ingredientLines), joinLines(instructionLines), warnings);
    }

    private boolean looksLikeIngredientLine(String line) {
        String normalized = line.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }
        if (INGREDIENT_LINE_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        return normalized.contains("cup")
                || normalized.contains("gram")
                || normalized.contains("tbsp")
                || normalized.contains("tsp")
                || normalized.contains("kg")
                || normalized.contains("ml");
    }

    private int findHeadingIndex(List<String> lines, String... headings) {
        for (int i = 0; i < lines.size(); i++) {
            String normalized = lines.get(i).toLowerCase(Locale.ROOT).replace(":", "").trim();
            for (String heading : headings) {
                if (normalized.equals(heading) || normalized.startsWith(heading + " ")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isHeading(String line) {
        String normalized = line.toLowerCase(Locale.ROOT).replace(":", "").trim();
        return normalized.equals("ingredients")
                || normalized.equals("ingredient")
                || normalized.equals("instructions")
                || normalized.equals("direction")
                || normalized.equals("directions")
                || normalized.equals("method")
                || normalized.equals("steps")
                || normalized.equals("bereiding");
    }

    private List<String> splitNonEmptyLines(String rawText) {
        List<String> lines = new ArrayList<>();
        for (String line : rawText.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    private String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        return String.join("\n", lines).trim();
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record HeuristicSection(String ingredients, String instructions, List<String> warnings) {
    }
}
