package be.lutske.leolegacy.domain.category;

import be.lutske.leolegacy.domain.category.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    Category save(Category category);
    void deleteById(Long id);
    List<Category> findByName(String name);
}