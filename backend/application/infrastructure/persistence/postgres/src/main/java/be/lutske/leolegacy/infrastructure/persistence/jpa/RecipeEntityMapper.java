package be.lutske.leolegacy.infrastructure.persistence.jpa;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;

import java.util.Arrays;
import java.util.List;

final class RecipeEntityMapper {

    private RecipeEntityMapper() {
    }

    static Recipe toDomain(RecipeEntity entity) {
        return new Recipe(
                entity.getId(),
                entity.getTitle(),
                splitIngredients(entity.getIngredients()),
                entity.getPreparation(),
                CategoryEntityMapper.toDomain(entity.getCategory()),
                entity.getViewCount(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getImportMetadata()
        );
    }

    static RecipeEntity fromImportDraft(RecipeImportDraft draft, CategoryEntity category) {
        var entity = new RecipeEntity();
        entity.setTitle(draft.title());
        entity.setIngredients(String.join("|", draft.ingredients()));
        entity.setPreparation(draft.preparation());
        entity.setCategory(category);
        entity.setViewCount(0);
        entity.setSource(draft.source());
        entity.setCreatedAt(draft.createdAt());
        entity.setImportMetadata(draft.importMetadata());
        return entity;
    }

    private static List<String> splitIngredients(String ingredients) {
        if (ingredients == null || ingredients.isBlank()) {
            return List.of();
        }
        return Arrays.stream(ingredients.split("\\|"))
                .filter(value -> !value.isBlank())
                .toList();
    }
}
