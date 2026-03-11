package be.lutske.leolegacy.infrastructure.persistence.mapper;

import be.lutske.leolegacy.domain.model.Category;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;

/**
 * Maps between domain {@link Category} and JPA {@link CategoryEntity}.
 */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        return new Category(entity.getId(), entity.getName());
    }
}
