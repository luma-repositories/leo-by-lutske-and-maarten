package be.lutske.leolegacy.usecases;

import be.lutske.leolegacy.domain.ExtractionResult;
import be.lutske.leolegacy.domain.RecipeExtractionException;

public interface RecipeExtractionRepository {
    ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType) throws RecipeExtractionException;
}