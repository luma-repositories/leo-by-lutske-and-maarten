package be.lutske.leolegacy.domain.recipe;

import java.util.Objects;

public class Ingredients {
    private final String value;

    public Ingredients(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredients that = (Ingredients) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}