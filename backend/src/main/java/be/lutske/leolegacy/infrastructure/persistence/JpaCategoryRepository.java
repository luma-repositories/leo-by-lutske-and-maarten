package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryName;
import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryName;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.domain.valueobject.CategoryId;
import be.lutske.leolegacy.domain.repository.CategoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaCategoryRepository implements CategoryRepository {
    
    @Inject
    be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository panacheRepository;

    @Override
    public Optional<Category> findById(CategoryId id) {
        return panacheRepository.findByIdOptional(Long.parseLong(id.value()))
                .map(this::mapToDomain);
    }

    @Override
    public List<Category> findAll() {
        return panacheRepository.listAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void save(Category category) {
        CategoryEntity entity = mapToEntity(category);
        panacheRepository.persist(entity);
    }

    @Override
    public void delete(CategoryId id) {
        panacheRepository.deleteById(Long.parseLong(id.value()));
    }

    Category mapToDomain(CategoryEntity entity) {
        return new Category(new CategoryId(String.valueOf(entity.getId())), new CategoryName(entity.getName()));
    }

    CategoryEntity mapToEntity(Category domain) {
        return new CategoryEntity(Long.parseLong(domain.getId().getValue()), domain.getName().getValue());
    }
}
}