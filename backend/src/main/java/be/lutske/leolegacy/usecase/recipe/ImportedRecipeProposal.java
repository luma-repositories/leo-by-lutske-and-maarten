package be.lutske.leolegacy.usecase.recipe;

import java.util.List;

public record ImportedRecipeProposal(
        String title,
        String description,
        String servings,
        List<String> ingredients,
        String preparation,
        String source,
        List<String> tags,
        Long categoryId,
        String notes
) {
    public ImportedRecipeProposal {
        ingredients = ingredients == null ? null : List.copyOf(ingredients);
        tags = tags == null ? null : List.copyOf(tags);
    }
}
