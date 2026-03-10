package be.lutske.leolegacy.application.service;

/**
 * Exception thrown when recipe extraction fails at the provider/model level.
 */
public class RecipeExtractionException extends RuntimeException {

    public RecipeExtractionException(String message) {
        super(message);
    }

    public RecipeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
