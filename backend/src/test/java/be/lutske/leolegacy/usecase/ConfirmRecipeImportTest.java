package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.Category;
import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.port.CategoryRepository;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the ConfirmRecipeImport use case.
 * Tests pure business logic — no framework, no database.
 */
class ConfirmRecipeImportTest {

    private RecipeRepository recipeRepository;
    private CategoryRepository categoryRepository;
    private ConfirmRecipeImport useCase;

    @BeforeEach
    void setup() {
        recipeRepository = mock(RecipeRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        useCase = new ConfirmRecipeImport(recipeRepository, categoryRepository);

        when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Category(15L, "Imported")));
        when(recipeRepository.save(any(Recipe.class)))
                .thenAnswer(inv -> {
                    Recipe r = inv.getArgument(0);
                    r.setId(42L);
                    return r;
                });
    }

    @Test
    void successfulImportReturnsRecipeWithId() {
        var request = new ConfirmRecipeImport.ConfirmRequest(
                "Test Recipe", List.of("flour", "eggs"), "Mix and bake.",
                null, null, null);

        Recipe result = useCase.execute(request);

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("Test Recipe", result.getTitle());
        assertEquals(List.of("flour", "eggs"), result.getIngredients());
    }

    @Test
    void missingTitleThrowsException() {
        var request = new ConfirmRecipeImport.ConfirmRequest(
                null, List.of("flour"), "Mix.", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void missingIngredientsThrowsException() {
        var request = new ConfirmRecipeImport.ConfirmRequest(
                "Title", null, "Mix.", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void missingPreparationThrowsException() {
        var request = new ConfirmRecipeImport.ConfirmRequest(
                "Title", List.of("flour"), null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void defaultCategoryIsUsedWhenNotSpecified() {
        var request = new ConfirmRecipeImport.ConfirmRequest(
                "Test", List.of("flour"), "Mix.", null, null, null);

        Recipe result = useCase.execute(request);

        assertEquals("Imported", result.getCategory().getName());
    }
}
