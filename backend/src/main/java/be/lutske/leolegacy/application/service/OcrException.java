package be.lutske.leolegacy.application.service;

/**
 * Exception thrown when OCR processing fails.
 */
public class OcrException extends RuntimeException {

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
