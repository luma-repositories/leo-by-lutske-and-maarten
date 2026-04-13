package be.lutske.leolegacy.usecase.recipe;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;
import be.lutske.leolegacy.port.out.CategoryQueryPort;
import be.lutske.leolegacy.port.out.RecipeCommandPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmRecipeImportUseCaseTest {

    @Test
    void usesUserOverridesAndAppendsNotes() {
        var useCase = new ConfirmRecipeImportUseCase(new CapturingRecipeCommandPort(), new FixedCategoryQueryPort());

        Recipe recipe = useCase.execute(new ConfirmRecipeImportCommand(
                "raw-output",
                new ImportedRecipeProposal("Original", null, null, List.of("old"), "old prep", null, null, null, null),
                new UserRecipeOverrides("Updated", List.of("200 g flour"), "Mix well.", null, "Serve warm", null, null)
        ));

        assertEquals("Updated", recipe.title());
        assertEquals(List.of("200 g flour"), recipe.ingredients());
        assertEquals("Mix well.\n\nNotes: Serve warm", recipe.preparation());
        assertEquals(15L, recipe.category().id());
    }

    @Test
    void fallsBackToDefaultImportCategoryWhenRequestedCategoryDoesNotExist() {
        var recipeCommandPort = new CapturingRecipeCommandPort();
        var useCase = new ConfirmRecipeImportUseCase(recipeCommandPort, new FixedCategoryQueryPort());

        useCase.execute(new ConfirmRecipeImportCommand(
                null,
                new ImportedRecipeProposal("Recipe", null, null, List.of("item"), "prep", null, null, 999L, null),
                null
        ));

        assertEquals(15L, recipeCommandPort.savedDraft.categoryId());
    }

    @Test
    void rejectsMissingTitle() {
        var useCase = new ConfirmRecipeImportUseCase(new CapturingRecipeCommandPort(), new FixedCategoryQueryPort());

        assertThrows(RuntimeException.class, () -> useCase.execute(new ConfirmRecipeImportCommand(
                null,
                new ImportedRecipeProposal(null, null, null, List.of("item"), "prep", null, null, null, null),
                null
        )));
    }

    private static final class FixedCategoryQueryPort implements CategoryQueryPort {

        @Override
        public List<Category> findAllOrderedByName() {
            return List.of();
        }

        @Override
        public Optional<Category> findById(long id) {
            if (id == 15L) {
                return Optional.of(new Category(15L, "Imported"));
            }
            return Optional.empty();
        }
    }

    private static final class CapturingRecipeCommandPort implements RecipeCommandPort {

        private RecipeImportDraft savedDraft;

        @Override
        public Recipe saveImportedRecipe(RecipeImportDraft draft) {
            this.savedDraft = draft;
            return new Recipe(
                    100L,
                    draft.title(),
                    draft.ingredients(),
                    draft.preparation(),
                    new Category(draft.categoryId(), "Imported"),
                    0,
                    draft.source(),
                    Instant.now(),
                    draft.importMetadata()
            );
        }
    }
}
