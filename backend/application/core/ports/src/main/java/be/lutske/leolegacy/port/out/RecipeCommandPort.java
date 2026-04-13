package be.lutske.leolegacy.port.out;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;

public interface RecipeCommandPort {

    Recipe saveImportedRecipe(RecipeImportDraft draft);
}
