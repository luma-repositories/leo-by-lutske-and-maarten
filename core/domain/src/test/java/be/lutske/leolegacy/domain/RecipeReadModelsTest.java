package be.lutske.leolegacy.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipeReadModelsTest {

    @Test
    void testCategory() {
        final var category = new Category(15L, "Imported");
        assertEquals(15L, category.id());
        assertEquals("Imported", category.name());
    }

    @Test
    void testCategoryWithLargeCount() {
        final var categoryWithCount = new CategoryWithCount(15L, "Imported", 3_000_000_000L);
        assertEquals(15L, categoryWithCount.id());
        assertEquals("Imported", categoryWithCount.name());
        assertEquals(3_000_000_000L, categoryWithCount.recipeCount());
    }

    @Test
    void testRecipeSummaryWithZeroViewCount() {
        final var recipeSummary = new RecipeSummary(1L, "Test Recipe", "Imported", 0);
        assertEquals(1L, recipeSummary.id());
        assertEquals("Test Recipe", recipeSummary.title());
        assertEquals("Imported", recipeSummary.categoryName());
        assertEquals(0, recipeSummary.viewCount());
    }

    @Test
    void testRecipeDetailWithEmptyIngredients() {
        final var detail = new RecipeDetail(
            1L,
            "Empty Recipe",
            List.of(),
            "Mix.\nBake.",
            15L,
            "Imported",
            0
        );
        assertEquals(1L, detail.id());
        assertEquals("Empty Recipe", detail.title());
        assertEquals(List.of(), detail.ingredients());
        assertEquals("Mix.\nBake.", detail.preparation());
        assertEquals(15L, detail.categoryId());
        assertEquals("Imported", detail.categoryName());
        assertEquals(0, detail.viewCount());
    }

    @Test
    void testRecipeDetailWithIngredients() {
        final var detail = new RecipeDetail(
            1L,
            "Flour Recipe",
            List.of("  flour", "4 eggs"),
            "Mix.\nBake.",
            15L,
            "Imported",
            100
        );
        assertEquals(1L, detail.id());
        assertEquals("Flour Recipe", detail.title());
        assertEquals(List.of("  flour", "4 eggs"), detail.ingredients());
        assertEquals("Mix.\nBake.", detail.preparation());
        assertEquals(15L, detail.categoryId());
        assertEquals("Imported", detail.categoryName());
        assertEquals(100, detail.viewCount());
    }
}
