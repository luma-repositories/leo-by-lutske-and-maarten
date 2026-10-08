package be.lutske.leolegacy.domain;

public class ImportCategoryNotFoundException extends RuntimeException {
    public ImportCategoryNotFoundException() {
        super("Default import category not found");
    }
}