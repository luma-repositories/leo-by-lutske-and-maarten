package be.lutske.leolegacy.domain.exception;

import be.lutske.leolegacy.domain.category.CategoryId;

public class CategoryNotFoundException extends RuntimeException {
    private final CategoryId categoryId;

    public CategoryNotFoundException(CategoryId categoryId) {
        super("Category not found: " + categoryId.getValue());
        this.categoryId = categoryId;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }
}