package be.lutske.leolegacy.infrastructure.persistence.mapper;

import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Maps between domain {@link Recipe} and JPA {@link RecipeEntity}.
 */
public final class RecipeMapper {

    private RecipeMapper() {
    }

    public static Recipe toDomain(RecipeEntity entity) {
        List<String> ingredients = entity.getIngredients() != null
                ? Arrays.stream(entity.getIngredients().split("\\|"))
                    .filter(s -> !s.isBlank())
                    .toList()
                : List.of();

        return new Recipe(
                entity.getId(),
                entity.getTitle(),
                ingredients,
                entity.getPreparation(),
                CategoryMapper.toDomain(entity.getCategory()),
                entity.getViewCount(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getImportMetadata()
        );
    }

    public static void toEntity(Recipe domain, RecipeEntity entity, CategoryEntity categoryEntity) {
        entity.setTitle(domain.getTitle());
        entity.setIngredients(domain.getIngredients() != null ? String.join("|", domain.getIngredients()) : "");
        entity.setPreparation(domain.getPreparation() != null ? domain.getPreparation() : "");
        entity.setCategory(categoryEntity);
        entity.setViewCount(domain.getViewCount());
        entity.setSource(domain.getSource());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setImportMetadata(domain.getImportMetadata());
    }
}
