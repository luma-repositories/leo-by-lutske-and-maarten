package be.lutske.leolegacy.interfaceadapter.rest.mapper;

import be.lutske.leolegacy.domain.entity.RecipeEntity;
import be.lutske.leolegacy.interfaceadapter.rest.dto.RecipeDetailResponse;
import be.lutske.leolegacy.interfaceadapter.rest.dto.RecipeSummaryResponse;

public class RecipeMapper {
    public static RecipeDetailResponse toDetailDto(RecipeEntity entity) {
        return new RecipeDetailResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getIngredients().stream().map(ingredient -> ingredient.getName() + " - " + ingredient.getAmount()).toList(),
            entity.getInstructions(),
            entity.getCategory().getId(),
            entity.getCategory().getName()
        );
    }

    public static RecipeSummaryResponse toSummaryDto(RecipeEntity entity) {
        return new RecipeSummaryResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getCategory().getId(),
            entity.getCategory().getName()
        );
    }

    public static RecipeEntity toEntity(RecipeDetailResponse dto) {
        return new RecipeEntity(
            dto.id(),
            dto.title(),
            dto.description(),
            dto.ingredients().stream().map(ingredient -> {
                String[] parts = ingredient.split(" - ");
                return new Ingredient(parts[0], parts[1]);
            }).toList(),
            dto.instructions(),
            new Category(dto.categoryId(), dto.categoryName())
        );
    }
