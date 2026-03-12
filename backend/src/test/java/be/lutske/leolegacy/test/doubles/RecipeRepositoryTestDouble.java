package be.lutske.leolegacy.test.doubles;

import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import be.lutske.leolegacy.domain.recipe.Recipe;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeRepositoryTestDouble implements RecipeRepository {
    private final Map<Long, Recipe> inMemoryStore = new HashMap<>();
    private Recipe lastSavedRecipe;
    private Recipe lastSavedRecipe;
    private Recipe lastSavedRecipe;
    private Recipe lastSavedRecipe;
    private Recipe lastSavedRecipe;
    private Recipe lastSavedRecipe;

    @Override
    public Recipe save(Recipe recipe) {
        lastSavedRecipe = recipe;
        inMemoryStore.put(recipe.getId(), recipe);
        return recipe;
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        return Optional.ofNullable(inMemoryStore.get(id));
    }

    @Override
    public List<Recipe> findAll() {
        return new ArrayList<>(inMemoryStore.values());
    }

    @Override
    public void deleteById(Long id) {
        inMemoryStore.remove(id);
    }

    public Recipe getLastSavedRecipe() {
        return lastSavedRecipe;
    }
}