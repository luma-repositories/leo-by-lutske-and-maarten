package be.lutske.leolegacy.application.port;

public interface OcrPort {
    String extractText(byte[] imageBytes);
}
