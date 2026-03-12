package be.lutske.leolegacy.domain.valueobject;

import java.util.Objects;

public record Ingredient(String name, String amount, String unit) {
    
    public Ingredient {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be null or empty");
        }
    }

    // Accessor methods (explicitly defined as per requirements)
    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }
}