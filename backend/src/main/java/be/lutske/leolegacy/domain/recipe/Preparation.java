package be.lutske.leolegacy.domain.recipe;

import java.util.Objects;

public class Preparation {
    private final String value;

    public Preparation(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Preparation cannot be empty");
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
        Preparation that = (Preparation) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}