package be.lutske.leolegacy.domain;

public record RecipeImportOutcome(
        RecipeDetail createdRecipe,
        RecipeImportAnalysis analysis
) {
    public static RecipeImportOutcome created(RecipeDetail recipe) {
        return new RecipeImportOutcome(recipe, null);
    }

    public static RecipeImportOutcome needsMoreInfo(RecipeImportAnalysis analysis) {
        return new RecipeImportOutcome(null, analysis);
    }

    public boolean needsMoreInfo() {
        return analysis != null;
    }
}
