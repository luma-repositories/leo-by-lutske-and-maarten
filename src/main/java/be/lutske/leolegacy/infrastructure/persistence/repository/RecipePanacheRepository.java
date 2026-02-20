package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecipePanacheRepository implements PanacheRepository<RecipeEntity> {
}
