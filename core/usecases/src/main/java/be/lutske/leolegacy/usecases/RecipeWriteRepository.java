package be.lutske.leolegacy.usecases;

import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.NewRecipe;

public interface RecipeWriteRepository {
    RecipeDetail save(NewRecipe recipe);
}