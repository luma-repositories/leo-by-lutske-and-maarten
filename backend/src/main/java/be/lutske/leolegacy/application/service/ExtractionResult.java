package be.lutske.leolegacy.application.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of an LLM-based recipe extraction from an image.
 */
public record ExtractionResult(
        /** Extracted title, or null if the model could not determine it. */
        String title,

        /** Extracted description / summary, or null. */
        String description,

        /** Number of servings, or null if not found. */
        String servings,

        /** Extracted ingredients as a list of strings. */
        List<String> ingredients,

        /** Extracted preparation steps as a list of strings. */
        List<String> steps,

        /** Source attribution if found in the image. */
        String source,

        /** Tags / categories extracted from the recipe. */
        List<String> tags,

        /** Warnings from the model about uncertain or missing data. */
        List<String> warnings,

        /** Sanitized raw model response for debugging / UI support. */
        String rawModelResponse,

        /** The AI provider that was used (e.g. "openai", "claude", "vllm"). */
        String provider,

        /** The model name that was used (e.g. "gpt-4o", "claude-sonnet-4-20250514"). */
        String model
) {
    /**
     * Compact constructor — defaults warnings to empty list if null.
     */
    public ExtractionResult {
        if (warnings == null) {
            warnings = List.of();
        }
    }

    /**
     * No-arg constructor for convenience (all fields null/empty).
     */
    public ExtractionResult() {
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
