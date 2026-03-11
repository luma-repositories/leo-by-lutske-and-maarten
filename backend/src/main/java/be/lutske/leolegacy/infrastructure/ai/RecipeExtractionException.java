package be.lutske.leolegacy.infrastructure.ai;

public class RecipeExtractionException extends RuntimeException {

    public RecipeExtractionException(String message) {
        super(message);
    }

    public RecipeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
