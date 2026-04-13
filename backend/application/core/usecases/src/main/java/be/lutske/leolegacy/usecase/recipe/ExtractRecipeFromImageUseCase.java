package be.lutske.leolegacy.usecase.recipe;

import be.lutske.leolegacy.domain.recipe.RecipeExtraction;
import be.lutske.leolegacy.port.out.RecipeImageExtractionPort;

public class ExtractRecipeFromImageUseCase {

    private final RecipeImageExtractionPort recipeImageExtractionPort;

    public ExtractRecipeFromImageUseCase(RecipeImageExtractionPort recipeImageExtractionPort) {
        this.recipeImageExtractionPort = recipeImageExtractionPort;
    }

    public RecipeExtraction execute(byte[] imageBytes, String mimeType) {
        return recipeImageExtractionPort.extractRecipeFromImage(imageBytes, mimeType);
    }
}
