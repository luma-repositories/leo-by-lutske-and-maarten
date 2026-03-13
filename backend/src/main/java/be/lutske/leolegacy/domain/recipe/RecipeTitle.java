package be.lutske.leolegacy.domain.recipe;

public class RecipeTitle {
    private final String value;

    public RecipeTitle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title cannot be empty");
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
}