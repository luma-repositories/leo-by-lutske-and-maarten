package be.lutske.leolegacy.domain.valueobject;

import java.util.List;
import java.util.stream.Stream;

public record Ingredients(List<Ingredient> ingredients) {
    
    public Ingredients {
        if (ingredients == null) {
            throw new IllegalArgumentException("Ingredients list cannot be null");
        }
        ingredients = List.copyOf(ingredients);
    }

    // Business methods
    public int size() {
        return ingredients.size();
    }

    public boolean isEmpty() {
        return ingredients.isEmpty();
    }

    public Stream<Ingredient> stream() {
        return ingredients.stream();
    }
}