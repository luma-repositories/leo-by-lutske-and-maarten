package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RecipeRepository implements PanacheRepository<RecipeEntity> {

    public List<RecipeEntity> findByCategoryId(long categoryId) {
        return list("category.id", categoryId);
    }

    public List<RecipeEntity> findTopByViewCount(int limit) {
        return list("ORDER BY viewCount DESC").stream()
                .limit(limit)
                .toList();
    }
}
