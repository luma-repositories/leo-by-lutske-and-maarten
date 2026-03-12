package be.lutske.leolegacy.domain.valueobject;

public record RecipeTitle(String value) {

    public RecipeTitle {
        if (value == null) {
            throw new IllegalArgumentException("Recipe title must not be null");
        }

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title must not be empty");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Recipe title must not exceed 255 characters");
        }
    }
}