package be.lutske.leolegacy.port.out;

import be.lutske.leolegacy.domain.recipe.RecipeExtraction;

public interface RecipeImageExtractionPort {

    RecipeExtraction extractRecipeFromImage(byte[] imageBytes, String mimeType);
}
