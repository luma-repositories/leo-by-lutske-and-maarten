package be.lutske.leolegacy.domain.valueobject;

public record CategoryId(String value) {
    
    public CategoryId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
    }

    // Custom accessor method
    public String value() {
        return value;
    }
}