package be.lutske.leolegacy.infrastructure.persistence.inmemory;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;
import be.lutske.leolegacy.port.out.RecipeCommandPort;
import be.lutske.leolegacy.port.out.RecipeQueryPort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRecipeRepository implements RecipeQueryPort, RecipeCommandPort {

    private final Map<Long, Recipe> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final InMemoryCategoryRepository categoryRepository;

    public InMemoryRecipeRepository(InMemoryCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Recipe addRecipe(Recipe recipe) {
        store.put(recipe.id(), recipe);
        if (recipe.id() >= idGenerator.get()) {
            idGenerator.set(recipe.id() + 1);
        }
        return recipe;
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    @Override
    public List<Recipe> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Recipe> findByCategoryId(long categoryId) {
        return store.values().stream()
                .filter(r -> r.category().id() == categoryId)
                .toList();
    }

    @Override
    public List<Recipe> findTopByViewCount(int limit) {
        return store.values().stream()
                .sorted(Comparator.comparingInt(Recipe::viewCount).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public List<Recipe> findBySearchTerms(Collection<String> searchTerms, int limit) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            return List.of();
        }
        return store.values().stream()
                .filter(recipe -> {
                    String haystack = (recipe.title() + " " + recipe.category().name() + " " +
                                       recipe.preparation() + " " + String.join(" ", recipe.ingredients()))
                            .toLowerCase(Locale.ROOT);
                    return searchTerms.stream().anyMatch(haystack::contains);
                })
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<Recipe> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public long countByCategoryId(long categoryId) {
        return store.values().stream()
                .filter(r -> r.category().id() == categoryId)
                .count();
    }

    @Override
    public Recipe saveImportedRecipe(RecipeImportDraft draft) {
        long id = idGenerator.getAndIncrement();
        Category category = categoryRepository.findById(draft.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + draft.categoryId() + " not found"));
        var recipe = new Recipe(id, draft.title(), draft.ingredients(), draft.preparation(),
                category, 0, draft.source(), draft.createdAt(), draft.importMetadata());
        store.put(id, recipe);
        return recipe;
    }
}
