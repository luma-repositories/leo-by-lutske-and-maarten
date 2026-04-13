package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<CategoryEntity> {

    public List<CategoryEntity> findAllOrderedByName() {
        return list("ORDER BY name");
    }
}
