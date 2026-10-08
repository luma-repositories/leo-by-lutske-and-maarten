package be.lutske.leolegacy.domain;

import java.util.List;

public class ExtractionResult {
    private final String title;
    private final String description;
    private final String servings;
    private final List<String> ingredients;
    private final List<String> steps;
    private final String source;
    private final List<String> tags;
    private final List<String> warnings;
    private final String rawModelResponse;
    private final String provider;
    private final String model;

    public ExtractionResult(String title, String description, String servings,
                           List<String> ingredients, List<String> steps, String source,
                           List<String> tags, List<String> warnings,
                           String rawModelResponse, String provider, String model) {
        this.title = title;
        this.description = description;
        this.servings = servings;
        this.ingredients = ingredients == null ? List.of() : ingredients;
        this.steps = steps;
        this.source = source;
        this.tags = tags == null ? List.of() : tags;
        this.warnings = warnings == null ? List.of() : warnings;
        this.rawModelResponse = rawModelResponse;
        this.provider = provider;
        this.model = model;
    }

    public ExtractionResult() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public List<String> missingFields() {
        List<String> result = new java.util.ArrayList<>();
        if (title == null || title.isBlank()) {
            result.add("title");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            result.add("ingredients");
        }
        if (steps == null || steps.isEmpty()) {
            result.add("preparation");
        }
        return result;
    }

    public boolean isComplete() {
        return missingFields().isEmpty();
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String servings() {
        return servings;
    }

    public List<String> ingredients() {
        return ingredients;
    }

    public List<String> steps() {
        return steps;
    }

    public String source() {
        return source;
    }

    public List<String> tags() {
        return tags;
    }

    public List<String> warnings() {
        return warnings;
    }

    public String rawModelResponse() {
        return rawModelResponse;
    }

    public String provider() {
        return provider;
    }

    public String model() {
        return model;
    }
}