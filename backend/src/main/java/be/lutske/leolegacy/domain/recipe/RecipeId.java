package be.lutske.leolegacy.domain.recipe;

import java.util.Objects;

public final class RecipeId {
    private final String value;

    public RecipeId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeId recipeId = (RecipeId) o;
        return Objects.equals(value, recipeId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}