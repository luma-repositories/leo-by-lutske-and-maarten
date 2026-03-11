package be.lutske.leolegacy.domain.exception;

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
