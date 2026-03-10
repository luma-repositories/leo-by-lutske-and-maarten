package be.lutske.leolegacy.application.service;

import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ProposedRecipeDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses raw OCR text into a structured recipe proposal.
 *
 * Heuristics:
 * - Title: first non-empty line, or line in ALL CAPS, or line before "Ingredients"
 * - Ingredients: section between ingredient-heading and instruction-heading
 * - Instructions: section after instruction-heading
 * - If no headings found, attempts to detect ingredient-like lines (quantities/units)
 */
@ApplicationScoped
public class RecipeParserService {

    private static final List<String> INGREDIENT_HEADINGS = List.of(
            "ingredients", "ingredienten", "benodigdheden", "wat heb je nodig",
            "ingredient", "ingrediënten"
    );

    private static final List<String> INSTRUCTION_HEADINGS = List.of(
            "instructions", "directions", "method", "steps", "preparation",
            "bereiding", "bereidingswijze", "werkwijze", "stappen"
    );

    private static final Pattern INGREDIENT_LINE_PATTERN = Pattern.compile(
            "^\\s*(\\d|½|¼|¾|⅓|⅔|•|[-–—*]|\\d+[.,/]\\d+)\\s*.*",
            Pattern.CASE_INSENSITIVE
    );

    public record ParseResult(
            ProposedRecipeDto proposedRecipe,
            List<String> missingFields,
            List<String> parseWarnings
    ) {}

    /**
     * Parse raw OCR text into a structured recipe proposal.
     */
    public ParseResult parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParseResult(
                    new ProposedRecipeDto(null, null, null, null, null),
                    List.of("title", "ingredients", "preparation"),
                    List.of("OCR produced no text")
            );
        }

        List<String> lines = Arrays.stream(rawText.split("\n"))
                .map(String::trim)
                .toList();
        var warnings = new ArrayList<String>();

        Integer ingredientStart = findHeadingIndex(lines, INGREDIENT_HEADINGS);
        Integer instructionStart = findHeadingIndex(lines, INSTRUCTION_HEADINGS);

        String title;
        List<String> ingredients;
        String preparation;

        if (ingredientStart != null && instructionStart != null && instructionStart > ingredientStart) {
            title = extractTitle(lines, ingredientStart);
            ingredients = extractSection(lines, ingredientStart + 1, instructionStart);
            preparation = extractTextBlock(lines, instructionStart + 1, lines.size());
        } else if (ingredientStart != null) {
            title = extractTitle(lines, ingredientStart);
            List<String> remaining = lines.subList(ingredientStart + 1, lines.size());
            Integer splitPoint = findInstructionBoundary(remaining);
            if (splitPoint != null) {
                ingredients = remaining.subList(0, splitPoint).stream()
                        .filter(s -> !s.isBlank()).toList();
                preparation = String.join("\n", remaining.subList(splitPoint, remaining.size()).stream()
                        .filter(s -> !s.isBlank()).toList());
            } else {
                ingredients = remaining.stream().filter(s -> !s.isBlank()).toList();
                preparation = null;
                warnings.add("Could not find instructions section — all text after ingredients heading treated as ingredients");
            }
        } else if (instructionStart != null) {
            title = extractTitle(lines, instructionStart);
            ingredients = null;
            preparation = extractTextBlock(lines, instructionStart + 1, lines.size());
            warnings.add("Could not find ingredients section");
        } else {
            warnings.add("No section headings detected — using heuristic parsing");
            title = lines.stream().filter(s -> !s.isBlank()).findFirst().orElse(null);

            List<String> contentLines;
            if (title != null) {
                int titleIndex = lines.indexOf(title);
                contentLines = lines.subList(titleIndex + 1, lines.size()).stream()
                        .filter(s -> !s.isBlank()).toList();
            } else {
                contentLines = lines.stream().filter(s -> !s.isBlank()).toList();
            }

            var ingredientLines = contentLines.stream()
                    .filter(s -> INGREDIENT_LINE_PATTERN.matcher(s).matches()).toList();
            var otherLines = contentLines.stream()
                    .filter(s -> !INGREDIENT_LINE_PATTERN.matcher(s).matches()).toList();

            ingredients = ingredientLines.isEmpty() ? null : ingredientLines;
            preparation = otherLines.isEmpty() ? null : String.join("\n", otherLines);

            if (ingredients == null && preparation == null && !contentLines.isEmpty()) {
                return new ParseResult(
                        new ProposedRecipeDto(title, null, String.join("\n", contentLines), null, null),
                        buildMissingFields(title, null, String.join("\n", contentLines)),
                        new ArrayList<>(warnings) {{ add("Could not distinguish ingredients from instructions"); }}
                );
            }
        }

        return new ParseResult(
                new ProposedRecipeDto(title, ingredients, preparation, null, null),
                buildMissingFields(title, ingredients, preparation),
                warnings
        );
    }

    private Integer findHeadingIndex(List<String> lines, List<String> headings) {
        for (int i = 0; i < lines.size(); i++) {
            String normalized = lines.get(i).toLowerCase().replace(":", "").trim();
            for (String heading : headings) {
                if (normalized.equals(heading) || normalized.startsWith(heading + " ")) {
                    return i;
                }
            }
        }
        return null;
    }

    private String extractTitle(List<String> lines, int beforeIndex) {
        List<String> candidates = lines.subList(0, beforeIndex).stream()
                .filter(s -> !s.isBlank()).toList();

        // Prefer ALL CAPS line
        for (String line : candidates) {
            if (line.length() > 2 && line.equals(line.toUpperCase()) && line.chars().anyMatch(Character::isLetter)) {
                return line;
            }
        }
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private List<String> extractSection(List<String> lines, int fromIndex, int toIndex) {
        return lines.subList(fromIndex, toIndex).stream()
                .filter(s -> !s.isBlank()).toList();
    }

    private String extractTextBlock(List<String> lines, int fromIndex, int toIndex) {
        String text = String.join("\n", lines.subList(fromIndex, toIndex).stream()
                .filter(s -> !s.isBlank()).toList());
        return text.isBlank() ? null : text;
    }

    private Integer findInstructionBoundary(List<String> lines) {
        int lastIngredientIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).isBlank() && INGREDIENT_LINE_PATTERN.matcher(lines.get(i)).matches()) {
                lastIngredientIndex = i;
            }
        }
        if (lastIngredientIndex >= 0 && lastIngredientIndex < lines.size() - 1) {
            return lastIngredientIndex + 1;
        }
        return null;
    }

    private List<String> buildMissingFields(String title, List<String> ingredients, String preparation) {
        var missing = new ArrayList<String>();
        if (title == null || title.isBlank()) missing.add("title");
        if (ingredients == null || ingredients.isEmpty()) missing.add("ingredients");
        if (preparation == null || preparation.isBlank()) missing.add("preparation");
        return missing;
    }
}
