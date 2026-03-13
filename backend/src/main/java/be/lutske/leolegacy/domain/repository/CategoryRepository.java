package be.lutske.leolegacy.domain.repository;

import be.lutske.leolegacy.domain.entity.Category;
import be.lutske.leolegacy.domain.valueobject.CategoryId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
    void save(Category category);
    void delete(CategoryId id);
}