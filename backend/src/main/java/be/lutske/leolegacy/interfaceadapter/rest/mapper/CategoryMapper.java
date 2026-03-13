package be.lutske.leolegacy.interfaceadapter.rest.mapper;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.interfaceadapter.rest.dto.CategoryResponse;

public class CategoryMapper {
    public static CategoryResponse toDto(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public static Category toEntity(CategoryResponse dto) {
        return new Category(dto.id(), dto.name(), dto.description());
    }
