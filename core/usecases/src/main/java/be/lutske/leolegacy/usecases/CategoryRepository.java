package be.lutske.leolegacy.usecases;

import be.lutske.leolegacy.domain.Category;
import be.lutske.leolegacy.domain.CategoryWithCount;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<CategoryWithCount> listWithCounts();
    Optional<Category> findById(long categoryId);
}