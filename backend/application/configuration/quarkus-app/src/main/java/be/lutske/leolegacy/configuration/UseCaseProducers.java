package be.lutske.leolegacy.configuration;

import be.lutske.leolegacy.port.out.CategoryQueryPort;
import be.lutske.leolegacy.port.out.RecipeCommandPort;
import be.lutske.leolegacy.port.out.RecipeImageExtractionPort;
import be.lutske.leolegacy.port.out.RecipeQueryPort;
import be.lutske.leolegacy.usecase.category.ListCategoriesUseCase;
import be.lutske.leolegacy.usecase.chatbot.AnswerRecipeChatMessageUseCase;
import be.lutske.leolegacy.usecase.recipe.ConfirmRecipeImportUseCase;
import be.lutske.leolegacy.usecase.recipe.ExtractRecipeFromImageUseCase;
import be.lutske.leolegacy.usecase.recipe.GetRecipeUseCase;
import be.lutske.leolegacy.usecase.recipe.ListRecipesUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class UseCaseProducers {

    @Produces
    @ApplicationScoped
    public ListCategoriesUseCase listCategoriesUseCase(CategoryQueryPort categoryQueryPort,
                                                       RecipeQueryPort recipeQueryPort) {
        return new ListCategoriesUseCase(categoryQueryPort, recipeQueryPort);
    }

    @Produces
    @ApplicationScoped
    public ListRecipesUseCase listRecipesUseCase(RecipeQueryPort recipeQueryPort) {
        return new ListRecipesUseCase(recipeQueryPort);
    }

    @Produces
    @ApplicationScoped
    public GetRecipeUseCase getRecipeUseCase(RecipeQueryPort recipeQueryPort) {
        return new GetRecipeUseCase(recipeQueryPort);
    }

    @Produces
    @ApplicationScoped
    public ExtractRecipeFromImageUseCase extractRecipeFromImageUseCase(RecipeImageExtractionPort extractionPort) {
        return new ExtractRecipeFromImageUseCase(extractionPort);
    }

    @Produces
    @ApplicationScoped
    public ConfirmRecipeImportUseCase confirmRecipeImportUseCase(RecipeCommandPort recipeCommandPort,
                                                                  CategoryQueryPort categoryQueryPort) {
        return new ConfirmRecipeImportUseCase(recipeCommandPort, categoryQueryPort);
    }

    @Produces
    @ApplicationScoped
    public AnswerRecipeChatMessageUseCase answerRecipeChatMessageUseCase(RecipeQueryPort recipeQueryPort) {
        return new AnswerRecipeChatMessageUseCase(recipeQueryPort);
    }
}
