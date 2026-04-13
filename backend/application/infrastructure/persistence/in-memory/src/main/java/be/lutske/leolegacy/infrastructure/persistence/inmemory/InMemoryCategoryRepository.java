package be.lutske.leolegacy.infrastructure.persistence.inmemory;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.port.out.CategoryQueryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCategoryRepository implements CategoryQueryPort {

    private final Map<Long, Category> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Category addCategory(long id, String name) {
        var category = new Category(id, name);
        store.put(id, category);
        if (id >= idGenerator.get()) {
            idGenerator.set(id + 1);
        }
        return category;
    }

    public Category addCategory(String name) {
        long id = idGenerator.getAndIncrement();
        var category = new Category(id, name);
        store.put(id, category);
        return category;
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    @Override
    public List<Category> findAllOrderedByName() {
        return store.values().stream()
                .sorted(Comparator.comparing(Category::name))
                .toList();
    }

    @Override
    public Optional<Category> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }
}
