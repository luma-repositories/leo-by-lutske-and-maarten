package be.lutske.leolegacy.domain;

public class RecipeImportValidationException extends RuntimeException {
    public RecipeImportValidationException(String message) {
        super(message);
    }
}