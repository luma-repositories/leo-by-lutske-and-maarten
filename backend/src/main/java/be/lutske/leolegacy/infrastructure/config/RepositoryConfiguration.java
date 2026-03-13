package be.lutske.leolegacy.infrastructure.config;

import be.lutske.leolegacy.domain.repository.RecipeRepository;
import be.lutske.leolegacy.infrastructure.persistence.JpaRecipeRepository;
import be.lutske.leolegacy.domain.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.JpaCategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RepositoryConfiguration {

    @Produces
    @ApplicationScoped
    public RecipeRepository recipeRepository() {
        return new JpaRecipeRepository();
    }

    @Produces
    @ApplicationScoped
    public CategoryRepository categoryRepository() {
        return new JpaCategoryRepository();
    }

    // This configuration binds domain interfaces to infrastructure implementations
    // using CDI producers, ensuring application-scoped lifecycle management
}