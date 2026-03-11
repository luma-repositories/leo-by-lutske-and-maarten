package be.lutske.leolegacy.infrastructure.persistence.jpa;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.port.out.CategoryQueryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryPersistenceAdapter implements CategoryQueryPort {

    private final CategoryRepository categoryRepository;

    public CategoryPersistenceAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAllOrderedByName() {
        return categoryRepository.findAllOrderedByName().stream()
                .map(CategoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(long id) {
        return Optional.ofNullable(categoryRepository.findById(id))
                .map(CategoryEntityMapper::toDomain);
    }
}
