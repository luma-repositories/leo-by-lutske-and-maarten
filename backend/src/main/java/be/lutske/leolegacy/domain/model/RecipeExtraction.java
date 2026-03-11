package be.lutske.leolegacy.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of an LLM-based recipe extraction from an image.
 * Pure domain value object — no framework dependencies.
 */
public record RecipeExtraction(
        String title,
        String description,
        String servings,
        List<String> ingredients,
        List<String> steps,
        String source,
        List<String> tags,
        List<String> warnings,
        String rawModelResponse,
        String provider,
        String model
) {
    public RecipeExtraction {
        if (warnings == null) {
            warnings = List.of();
        }
    }

    public RecipeExtraction() {
        this(null, null, null, null, null, null, null, List.of(), null, null, null);
    }

    /**
     * Returns the list of required fields that are missing or empty.
     */
    public List<String> missingFields() {
        var missing = new ArrayList<String>();
        if (title == null || title.isBlank()) missing.add("title");
        if (ingredients == null || ingredients.isEmpty()) missing.add("ingredients");
        if (steps == null || steps.isEmpty()) missing.add("preparation");
        return missing;
    }

    /**
     * Returns true if all required fields (title, ingredients, steps) are present.
     */
    public boolean isComplete() {
        return missingFields().isEmpty();
    }
}
