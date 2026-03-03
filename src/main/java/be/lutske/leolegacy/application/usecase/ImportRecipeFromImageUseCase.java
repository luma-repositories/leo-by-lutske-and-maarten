package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.OcrPort;
import be.lutske.leolegacy.application.port.RecipeCommandPort;
import be.lutske.leolegacy.application.port.RecipeImportParserPort;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeImportAnalysis;
import be.lutske.leolegacy.domain.RecipeImportOutcome;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImportRecipeFromImageUseCase {

    private final OcrPort ocrPort;
    private final RecipeImportParserPort parserPort;
    private final RecipeCommandPort recipeCommandPort;

    public ImportRecipeFromImageUseCase(
            OcrPort ocrPort,
            RecipeImportParserPort parserPort,
            RecipeCommandPort recipeCommandPort
    ) {
        this.ocrPort = ocrPort;
        this.parserPort = parserPort;
        this.recipeCommandPort = recipeCommandPort;
    }

    public RecipeImportOutcome execute(byte[] imageBytes) {
        String rawText = ocrPort.extractText(imageBytes);
        RecipeImportAnalysis analysis = parserPort.parse(rawText);
        if (analysis.needsMoreInfo()) {
            return RecipeImportOutcome.needsMoreInfo(analysis);
        }

        RecipeDetail createdRecipe = recipeCommandPort.createImportedRecipe(analysis.proposedRecipe(), rawText, null);
        return RecipeImportOutcome.created(createdRecipe);
    }
}
