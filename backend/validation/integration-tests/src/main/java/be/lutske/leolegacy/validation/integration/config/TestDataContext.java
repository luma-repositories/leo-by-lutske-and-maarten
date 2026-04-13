package be.lutske.leolegacy.validation.integration.config;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeExtraction;
import be.lutske.leolegacy.infrastructure.persistence.inmemory.InMemoryCategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.inmemory.InMemoryRecipeRepository;
import be.lutske.leolegacy.port.out.CategoryQueryPort;
import be.lutske.leolegacy.port.out.RecipeCommandPort;
import be.lutske.leolegacy.port.out.RecipeImageExtractionPort;
import be.lutske.leolegacy.port.out.RecipeQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class TestDataContext {

    private final InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();
    private final InMemoryRecipeRepository recipeRepo = new InMemoryRecipeRepository(categoryRepo);

    @Produces
    @ApplicationScoped
    public CategoryQueryPort categoryQueryPort() {
        return categoryRepo;
    }

    @Produces
    @ApplicationScoped
    public RecipeQueryPort recipeQueryPort() {
        return recipeRepo;
    }

    @Produces
    @ApplicationScoped
    public RecipeCommandPort recipeCommandPort() {
        return recipeRepo;
    }

    @Produces
    @ApplicationScoped
    public RecipeImageExtractionPort recipeImageExtractionPort() {
        return (imageBytes, mimeType) -> new RecipeExtraction(
                "Test Recipe", "A test recipe", "4 servings",
                List.of("100 g flour", "2 eggs"), List.of("Mix.", "Bake."),
                null, null, List.of(), "{}", "stub", "stub"
        );
    }

    public InMemoryCategoryRepository categoryRepo() {
        return categoryRepo;
    }

    public InMemoryRecipeRepository recipeRepo() {
        return recipeRepo;
    }

    public void reset() {
        categoryRepo.clear();
        recipeRepo.clear();
    }

    public void seedDefaults() {
        categoryRepo.addCategory(1, "Appetizers");
        categoryRepo.addCategory(2, "Soups");
        categoryRepo.addCategory(3, "Desserts");
        categoryRepo.addCategory(8, "Main dishes");
        categoryRepo.addCategory(15, "Imported");

        recipeRepo.addRecipe(new Recipe(1, "Tomato Soup", List.of("4 tomatoes", "1 onion", "salt"),
                "Cook tomatoes with onion. Blend. Season.", new Category(2, "Soups"), 5, null, Instant.now(), null));
        recipeRepo.addRecipe(new Recipe(2, "Chocolate Mousse", List.of("200 g dark chocolate", "4 eggs", "50 g sugar"),
                "Melt chocolate. Fold in whipped egg whites.", new Category(3, "Desserts"), 12, null, Instant.now(), null));
        recipeRepo.addRecipe(new Recipe(3, "Grilled Chicken", List.of("2 chicken breasts", "olive oil", "herbs"),
                "Marinate chicken. Grill until done.", new Category(8, "Main dishes"), 8, null, Instant.now(), null));
    }
}
