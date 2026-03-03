package be.lutske.leolegacy.application.port;

import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.RecipeDetail;

public interface RecipeCommandPort {
    RecipeDetail createImportedRecipe(ProposedRecipe proposedRecipe, String rawText, String notes);
}
