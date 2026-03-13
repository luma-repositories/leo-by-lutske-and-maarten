package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.repository.CategoryRepository;
import java.util.List;

public class ListCategoriesUseCase {
    private final CategoryRepository categoryRepository;

    public ListCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> execute() {
        return categoryRepository.findAll();
    }
}