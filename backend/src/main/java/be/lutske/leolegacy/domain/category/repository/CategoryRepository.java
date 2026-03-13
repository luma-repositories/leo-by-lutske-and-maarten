package be.lutske.leolegacy.domain.category.repository;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryId;
import java.util.List;

public interface CategoryRepository {
    Category findById(CategoryId id);
    List<Category> findAll();
}