package be.lutske.leolegacy.integration.test.builder;

import be.lutske.leolegacy.domain.recipe.command.CreateRecipeCommand;
import be.lutske.leolegacy.domain.recipe.command.CreateRecipeIngredientCommand;
import be.lutske.leolegacy.domain.recipe.command.CreateRecipeInstructionCommand;
import be.lutske.leolegacy.domain.recipe.command.UpdateRecipeCommand;
import be.lutske.leolegacy.domain.recipe.command.UpdateRecipeIngredientCommand;
import be.lutske.leolegacy.domain.recipe.command.UpdateRecipeInstructionCommand;
import be.lutske.leolegacy.domain.category.command.CreateCategoryCommand;
import be.lutske.leolegacy.domain.category.command.UpdateCategoryCommand;
import java.util.List;

public class IntegrationTestDataBuilder {
    
    public static CreateRecipeCommand.Builder aCreateRecipeCommand() {
        return CreateRecipeCommand.builder()
            .title("Integration Test Recipe")
            .description("Recipe created for integration testing")
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Flour", "2 cups", "cups"),
                new CreateRecipeIngredientCommand("Sugar", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Mix dry ingredients"),
                new CreateRecipeInstructionCommand(2, "Add wet ingredients"),
                new CreateRecipeInstructionCommand(3, "Bake for 30 minutes")
            ));
    }
    
    public static CreateCategoryCommand aCreateCategoryCommand() {
        return new CreateCategoryCommand(
            "Integration Test Category",
            "Category created for integration testing"
        );
    }
    
    public static CreateCategoryCommand aCreateCategoryCommand(String name) {
        return new CreateCategoryCommand(
            name,
            "Category created for integration testing: " + name
        );
    }
    
    public static UpdateRecipeCommand.Builder anUpdateRecipeCommand(RecipeId recipeId) {
        return UpdateRecipeCommand.builder()
            .recipeId(recipeId)
            .title("Updated Integration Test Recipe")
            .description("Updated recipe for integration testing")
            .ingredients(List.of(
                new UpdateRecipeIngredientCommand("Updated Flour", "3 cups", "cups"),
                new UpdateRecipeIngredientCommand("Updated Sugar", "1.5 cups", "cups")
            ))
            .instructions(List.of(
                new UpdateRecipeInstructionCommand(1, "Mix updated dry ingredients"),
                new UpdateRecipeInstructionCommand(2, "Add updated wet ingredients"),
                new UpdateRecipeInstructionCommand(3, "Bake for 35 minutes")
            ));
    }
    
    public static UpdateCategoryCommand anUpdateCategoryCommand(CategoryId categoryId) {
        return new UpdateCategoryCommand(
            categoryId,
            "Updated Integration Test Category",
            "Updated category for integration testing"
        );
    }
    
    public static List<CreateRecipeIngredientCommand> standardIngredients() {
        return List.of(
            new CreateRecipeIngredientCommand("Flour", "2 cups", "cups"),
            new CreateRecipeIngredientCommand("Sugar", "1 cup", "cup"),
            new CreateRecipeIngredientCommand("Eggs", "2 pieces", "pieces"),
            new CreateRecipeIngredientCommand("Milk", "1 cup", "cup")
        );
    }
    
    public static List<CreateRecipeInstructionCommand> standardInstructions() {
        return List.of(
            new CreateRecipeInstructionCommand(1, "Preheat oven to 350°F"),
            new CreateRecipeInstructionCommand(2, "Mix dry ingredients in a bowl"),
            new CreateRecipeInstructionCommand(3, "In separate bowl, beat eggs and add milk"),
            new CreateRecipeInstructionCommand(4, "Combine wet and dry ingredients"),
            new CreateRecipeInstructionCommand(5, "Pour into greased pan"),
            new CreateRecipeInstructionCommand(6, "Bake for 30-35 minutes")
        );
    }
