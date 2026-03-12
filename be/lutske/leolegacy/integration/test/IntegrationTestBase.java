package be.lutske.leolegacy.integration.test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestTransaction;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public abstract class IntegrationTestBase {
    
    @Inject
    protected RecipeRepository recipeRepository;
    
    @Inject
    protected CategoryRepository categoryRepository;
    
    @Inject
    protected CreateRecipeUseCase createRecipeUseCase;
    
    @Inject
    protected UpdateRecipeUseCase updateRecipeUseCase;
    
    @Inject
    protected DeleteRecipeUseCase deleteRecipeUseCase;
    
    @Inject
    protected FindRecipeUseCase findRecipeUseCase;
    
    @Inject
    protected CreateCategoryUseCase createCategoryUseCase;
    
    @Inject
    protected UpdateCategoryUseCase updateCategoryUseCase;
    
    @Inject
    protected DeleteCategoryUseCase deleteCategoryUseCase;
    
    @Inject
    protected FindCategoryUseCase findCategoryUseCase;
    
    @BeforeEach
    void setupIntegrationTest() {
        cleanDatabase();
    }
    
    @AfterEach
    void cleanupIntegrationTest() {
        cleanDatabase();
    }
    
    private void cleanDatabase() {
        // Clean test data in proper order (recipes first, then categories)
        recipeRepository.findAll().forEach(recipe -> 
            recipeRepository.deleteById(recipe.getId()));
        categoryRepository.findAll().forEach(category -> 
            categoryRepository.deleteById(category.getId()));
    }
    
    protected Category createTestCategory(String name) {
        CreateCategoryCommand command = new CreateCategoryCommand(name, "Test description for " + name);
        return createCategoryUseCase.execute(command);
    }
    
    protected Category createTestCategory(String name, String description) {
        CreateCategoryCommand command = new CreateCategoryCommand(name, description);
        return createCategoryUseCase.execute(command);
    }
    
    protected Recipe createTestRecipe(String title) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Test Ingredient", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected Recipe createTestRecipe(String title, CategoryId categoryId) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .categoryId(categoryId)
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Test Ingredient", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected Recipe createTestRecipeWithIngredients(String title, List<CreateRecipeIngredientCommand> ingredients) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .ingredients(ingredients)
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected void assertRecipeExistsInDatabase(RecipeId recipeId) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        assertTrue(recipe.isPresent(), "Expected recipe with ID " + recipeId + " to exist in database");
    }
    
    protected void assertRecipeDoesNotExistInDatabase(RecipeId recipeId) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        assertFalse(recipe.isPresent(), "Expected recipe with ID " + recipeId + " to not exist in database");
    }
    
    protected void assertCategoryExistsInDatabase(CategoryId categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        assertTrue(category.isPresent(), "Expected category with ID " + categoryId + " to exist in database");
    }
    
    protected void assertCategoryDoesNotExistInDatabase(CategoryId categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        assertFalse(category.isPresent(), "Expected category with ID " + categoryId + " to not exist in database");
    }
    
    protected void assertDatabaseCounts(int expectedRecipes, int expectedCategories) {
        List<Recipe> recipes = recipeRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        
        assertEquals(expectedRecipes, recipes.size(), 
            "Expected " + expectedRecipes + " recipes in database but found " + recipes.size());
        assertEquals(expectedCategories, categories.size(), 
            "Expected " + expectedCategories + " categories in database but found " + categories.size());
    }
