package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final EntityManager entityManager;

    public CategoryRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Category> findById(Long id) {
        CategoryEntity entity = entityManager.find(CategoryEntity.class, id);
        return entity != null ? Optional.of(toDomain(entity)) : Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        TypedQuery<CategoryEntity> query = entityManager.createQuery("SELECT e FROM CategoryEntity e", CategoryEntity.class);
        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = toEntity(category);
        entity = entityManager.merge(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        CategoryEntity entity = entityManager.find(CategoryEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<Category> findByName(String name) {
        TypedQuery<CategoryEntity> query = entityManager.createQuery(
                "SELECT e FROM CategoryEntity e WHERE e.name LIKE :name",
                CategoryEntity.class
        );
        query.setParameter("name", "%" + name + "%");
        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    private CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId());
        entity.setName(category.getName());
        return entity;
    }

    private Category toDomain(CategoryEntity entity) {
        return new Category()
                .setId(entity.getId())
                .setName(entity.getName());
    }
}