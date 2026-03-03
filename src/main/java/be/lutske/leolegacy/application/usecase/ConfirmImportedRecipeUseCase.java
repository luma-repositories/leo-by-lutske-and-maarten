package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.RecipeCommandPort;
import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeImportAnalysis;
import be.lutske.leolegacy.domain.RecipeImportOutcome;
import be.lutske.leolegacy.domain.UserRecipeOverrides;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ConfirmImportedRecipeUseCase {

    private final RecipeCommandPort recipeCommandPort;
    private final String defaultSource;

    public ConfirmImportedRecipeUseCase(RecipeCommandPort recipeCommandPort) {
        this.recipeCommandPort = recipeCommandPort;
        this.defaultSource = "Imported from image";
    }

    public RecipeImportOutcome execute(String rawText, ProposedRecipe proposedRecipe, UserRecipeOverrides userOverrides) {
        ProposedRecipe merged = merge(proposedRecipe, userOverrides);
        List<String> missingFields = validate(merged);

        if (!missingFields.isEmpty()) {
            RecipeImportAnalysis analysis = new RecipeImportAnalysis(
                    rawText,
                    merged,
                    missingFields,
                    List.of("Some required fields are still missing.")
            );
            return RecipeImportOutcome.needsMoreInfo(analysis);
        }

        RecipeDetail created = recipeCommandPort.createImportedRecipe(merged, rawText, normalize(userOverrides.notes()));
        return RecipeImportOutcome.created(created);
    }

    private ProposedRecipe merge(ProposedRecipe proposedRecipe, UserRecipeOverrides userOverrides) {
        return new ProposedRecipe(
                pick(userOverrides.title(), proposedRecipe.title()),
                pick(userOverrides.description(), proposedRecipe.description()),
                userOverrides.servings() != null ? userOverrides.servings() : proposedRecipe.servings(),
                pick(userOverrides.ingredients(), proposedRecipe.ingredients()),
                pick(userOverrides.instructions(), proposedRecipe.instructions()),
                pick(userOverrides.tags(), proposedRecipe.tags()),
                pick(userOverrides.source(), proposedRecipe.source(), defaultSource)
        );
    }

    private List<String> validate(ProposedRecipe recipe) {
        List<String> missing = new ArrayList<>();
        if (isBlank(recipe.title())) {
            missing.add("title");
        }
        if (isBlank(recipe.ingredients())) {
            missing.add("ingredients");
        }
        if (isBlank(recipe.instructions())) {
            missing.add("instructions");
        }
        return missing;
    }

    private String pick(String first, String second) {
        String firstValue = normalize(first);
        if (firstValue != null) {
            return firstValue;
        }
        return normalize(second);
    }

    private String pick(String first, String second, String fallback) {
        String value = pick(first, second);
        return value != null ? value : fallback;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
