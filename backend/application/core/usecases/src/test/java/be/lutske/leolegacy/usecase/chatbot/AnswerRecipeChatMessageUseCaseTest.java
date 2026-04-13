package be.lutske.leolegacy.usecase.chatbot;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.port.out.RecipeQueryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerRecipeChatMessageUseCaseTest {

    @Test
    void introducesLeonardoWhenNoSearchTermsAreProvided() {
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(List.of()));

        var reply = useCase.answer("Hello there", List.of());

        assertEquals("Leonardo", reply.author());
        assertTrue(reply.message().contains("I am Leonardo"));
        assertTrue(reply.recommendations().isEmpty());
    }

    @Test
    void returnsMatchingRecipesForIngredientQuestion() {
        var recipes = List.of(
                recipe(37L, "Pizza with mushrooms", List.of("Tomato sauce", "Mushrooms"), "Bake the pizza.", "Pasta"),
                recipe(38L, "Spaghetti with roasted bell pepper", List.of("Spaghetti", "Tomatoes"), "Cook and mix.", "Pasta")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("I would like a tomato based disch", List.of());

        assertEquals(2, reply.recommendations().size());
        assertEquals("Pizza with mushrooms", reply.recommendations().getFirst().title());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("tomato"));
    }

    @Test
    void matchesNaturalPhrasingForChickenQuestions() {
        var recipes = List.of(
                recipe(101L, "Gratinated chicken with pesto", List.of("Chicken fillets", "Tomatoes"), "Bake until golden.", "Main dishes")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("something with chicken", List.of());

        assertEquals(1, reply.recommendations().size());
        assertEquals("Gratinated chicken with pesto", reply.recommendations().getFirst().title());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("chicken"));
    }

    @Test
    void returnsHelpfulFallbackWhenNoRecipeMatches() {
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(List.of()));

        var reply = useCase.answer("I want dragon fruit dessert ideas", List.of());

        assertTrue(reply.message().contains("could not find a recipe"));
        assertTrue(reply.recommendations().isEmpty());
    }

    private static Recipe recipe(long id, String title, List<String> ingredients, String preparation, String categoryName) {
        return new Recipe(id, title, ingredients, preparation, new Category(1L, categoryName), 0, null, Instant.now(), null);
    }

    private static final class StubRecipeQueryPort implements RecipeQueryPort {

        private final List<Recipe> recipes;

        private StubRecipeQueryPort(List<Recipe> recipes) {
            this.recipes = recipes;
        }

        @Override
        public List<Recipe> findAll() {
            return recipes;
        }

        @Override
        public List<Recipe> findByCategoryId(long categoryId) {
            return List.of();
        }

        @Override
        public List<Recipe> findTopByViewCount(int limit) {
            return List.of();
        }

        @Override
        public List<Recipe> findBySearchTerms(Collection<String> searchTerms, int limit) {
            return recipes.stream()
                    .filter(recipe -> matches(recipe, searchTerms))
                    .limit(limit)
                    .toList();
        }

        @Override
        public Optional<Recipe> findById(long id) {
            return Optional.empty();
        }

        @Override
        public long countByCategoryId(long categoryId) {
            return 0;
        }

        private boolean matches(Recipe recipe, Collection<String> searchTerms) {
            String haystack = (recipe.title() + " " + recipe.category().name() + " " + recipe.preparation() + " " + String.join(" ", recipe.ingredients()))
                    .toLowerCase();
            return searchTerms.stream().anyMatch(haystack::contains);
        }
    }
}
