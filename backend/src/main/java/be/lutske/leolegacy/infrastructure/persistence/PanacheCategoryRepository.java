package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.domain.model.Category;
import be.lutske.leolegacy.domain.port.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.mapper.CategoryMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Panache-based adapter implementing the domain {@link CategoryRepository} port.
 */
@ApplicationScoped
public class PanacheCategoryRepository implements CategoryRepository, PanacheRepository<CategoryEntity> {

    @Override
    public List<Category> findAllOrderedByName() {
        return list("ORDER BY name").stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(long id) {
        CategoryEntity entity = find("id", id).firstResult();
        return Optional.ofNullable(entity).map(CategoryMapper::toDomain);
    }
}
