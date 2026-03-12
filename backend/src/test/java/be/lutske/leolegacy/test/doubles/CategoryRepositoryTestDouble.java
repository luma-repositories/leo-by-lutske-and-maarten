package be.lutske.leolegacy.test.doubles;

import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.domain.category.Category;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

public class CategoryRepositoryTestDouble implements CategoryRepository {
    private final Map<String, Category> inMemoryStore = new HashMap<>();
    private Category lastSavedCategory;
    private Category lastSavedCategory;

    @Override
    public Category save(Category category) {
        lastSavedCategory = category;
        inMemoryStore.put(category.getId(), category);
        return category;
    }

    @Override
    public Optional<Category> findById(String id) {
        return Optional.ofNullable(inMemoryStore.get(id));
    }

    @Override
    public void deleteById(String id) {
        inMemoryStore.remove(id);
    }

    public Category getLastSavedCategory() {
        return lastSavedCategory;
    }
}