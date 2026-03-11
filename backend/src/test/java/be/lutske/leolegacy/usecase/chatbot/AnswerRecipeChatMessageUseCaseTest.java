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
    void expandsCategoryAliasesForPastaQuestions() {
        var recipes = List.of(
                recipe(102L, "Salmon ravioli", List.of("Salmon", "Tomatoes"), "Serve with pasta sheets.", "Pasta"),
                recipe(103L, "Pizza with mushrooms", List.of("Tomato sauce", "Cheese"), "Bake the pizza.", "Pasta")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("I want pasta tonight", List.of());

        assertEquals(2, reply.recommendations().size());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("pasta"));
        assertTrue(reply.message().contains("pasta"));
    }

    @Test
    void matchesVeggieIntentToVegetableForwardRecipes() {
        var recipes = List.of(
                recipe(104L, "Zucchini Carpaccio with Dried Tomato", List.of("Zucchini", "Dried tomatoes", "Basil"), "Serve fresh.", "Starters")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("Show me a veggie idea", List.of());

        assertEquals(1, reply.recommendations().size());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("veggie"));
    }

    @Test
    void matchesLightIntentToFreshOrSoupStyleRecipes() {
        var recipes = List.of(
                recipe(105L, "Vegetable soup with salami", List.of("Tomatoes", "Broccoli"), "Serve as a light soup.", "Soups")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("I want something light", List.of());

        assertEquals(1, reply.recommendations().size());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("light"));
    }

    @Test
    void matchesSpicyIntentToSpicedRecipes() {
        var recipes = List.of(
                recipe(106L, "Devilishly delicious turkey drumsticks", List.of("Turkey", "Cayenne pepper"), "Season with spicy mustard mixture.", "Main dishes")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("Give me something spicy", List.of());

        assertEquals(1, reply.recommendations().size());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("spicy"));
    }

    @Test
    void matchesOvenDishIntentToBakedRecipes() {
        var recipes = List.of(
                recipe(107L, "Gratinated chicken with pesto", List.of("Chicken", "Tomatoes"), "Place the oven dish in the hot oven.", "Main dishes")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("Recommend an oven dish", List.of());

        assertEquals(1, reply.recommendations().size());
        assertTrue(reply.recommendations().getFirst().matchReason().contains("oven dish"));
    }

    @Test
    void ranksExactTitleMatchesAboveBroaderAliasMatches() {
        var recipes = List.of(
                recipe(108L, "Pizza with mushrooms", List.of("Tomato sauce"), "Bake the pizza.", "Pasta"),
                recipe(109L, "Salmon ravioli", List.of("Tomatoes"), "Serve with pasta sheets.", "Pasta")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer("pizza", List.of());

        assertEquals("Pizza with mushrooms", reply.recommendations().getFirst().title());
    }

    @Test
    void usesConversationMemoryForRefinements() {
        var recipes = List.of(
                recipe(110L, "Gratinated chicken with pesto", List.of("Chicken", "Tomatoes"), "Bake in the oven.", "Main dishes"),
                recipe(111L, "Zucchini Carpaccio with Dried Tomato", List.of("Zucchini", "Dried tomatoes"), "Serve fresh.", "Starters")
        );
        var useCase = new AnswerRecipeChatMessageUseCase(new StubRecipeQueryPort(recipes));

        var reply = useCase.answer(
                "make it vegetarian",
                List.of(new ChatMessageContext("user", "I would like a tomato based dish"))
        );

        assertTrue(reply.recommendations().size() >= 1);
        assertEquals("Zucchini Carpaccio with Dried Tomato", reply.recommendations().getFirst().title());
        assertTrue(reply.message().contains("vegetarian") || reply.message().contains("veggie"));
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
