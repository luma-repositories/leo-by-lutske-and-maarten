package be.lutske.leolegacy.domain.valueobject;

import java.util.Objects;

public record RecipeId(String value) {
    public RecipeId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe ID must not be null or empty");
        }
    }

    public String value() {
        return value;
    }
}