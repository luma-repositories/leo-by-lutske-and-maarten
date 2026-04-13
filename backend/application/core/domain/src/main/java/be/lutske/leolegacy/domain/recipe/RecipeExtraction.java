package be.lutske.leolegacy.domain.recipe;

import java.util.ArrayList;
import java.util.List;

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
        ingredients = ingredients == null ? null : List.copyOf(ingredients);
        steps = steps == null ? null : List.copyOf(steps);
        tags = tags == null ? null : List.copyOf(tags);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public List<String> missingFields() {
        var missing = new ArrayList<String>();
        if (title == null || title.isBlank()) {
            missing.add("title");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            missing.add("ingredients");
        }
        if (steps == null || steps.isEmpty()) {
            missing.add("preparation");
        }
        return missing;
    }

    public boolean isComplete() {
        return missingFields().isEmpty();
    }
}
