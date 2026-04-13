package be.lutske.leolegacy.infrastructure.persistence.jpa;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;

final class CategoryEntityMapper {

    private CategoryEntityMapper() {
    }

    static Category toDomain(CategoryEntity entity) {
        return new Category(entity.getId(), entity.getName());
    }
}
