package be.lutske.leolegacy.domain.category;

import java.util.Objects;

public class CategoryName {
    private final String value;

    public CategoryName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
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
        CategoryName that = (CategoryName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}