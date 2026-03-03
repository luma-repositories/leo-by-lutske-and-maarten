package be.lutske.leolegacy.application.port;

import be.lutske.leolegacy.domain.RecipeImportAnalysis;

public interface RecipeImportParserPort {
    RecipeImportAnalysis parse(String rawText);
}
