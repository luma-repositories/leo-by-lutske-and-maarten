package be.lutske.leolegacy.domain.category;

import be.lutske.leolegacy.domain.category.Category;

import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(CategoryId id);
    java.util.stream.Stream<Category> findAll();
    Category save(Category category);
    void delete(Category category);
}