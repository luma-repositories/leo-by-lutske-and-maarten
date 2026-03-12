package be.lutske.leolegacy.domain.valueobject;

public record PreparationSteps(String value) {
    
    public PreparationSteps {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Preparation steps cannot be null or empty");
        }
    }

    // Custom accessor method
    public String value() {
        return value;
    }
}