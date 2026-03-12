package be.lutske.leolegacy.domain.entity;

import be.lutske.leolegacy.domain.valueobject.RecipeId;
import be.lutske.leolegacy.domain.valueobject.RecipeTitle;
import be.lutske.leolegacy.domain.valueobject.Ingredients;
import be.lutske.leolegacy.domain.valueobject.PreparationSteps;
import be.lutske.leolegacy.domain.valueobject.ViewCount;
import be.lutske.leolegacy.domain.entity.Category;

import java.util.Objects;

public class Recipe {
    private final RecipeId id;
    private final RecipeTitle title;
    private final Ingredients ingredients;
    private final PreparationSteps preparationSteps;
    private final ViewCount viewCount;
    private final Category category;

    // Constructor for creating new recipes
    public Recipe(RecipeId id, RecipeTitle title, Ingredients ingredients, 
                 PreparationSteps preparationSteps, ViewCount viewCount, Category category) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
        this.viewCount = viewCount;
        this.category = category;
    }

    // Constructor for reconstituting existing recipes
    public Recipe(RecipeId id, RecipeTitle title, Ingredients ingredients, 
                 PreparationSteps preparationSteps, ViewCount viewCount, Category category, boolean reconstitute) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
        this.viewCount = viewCount;
        this.category = category;
    }

    public RecipeId getId() {
        return id;
    }

    public RecipeTitle getTitle() {
        return title;
    }

    public Ingredients getIngredients() {
        return ingredients;
    }

    public PreparationSteps getPreparationSteps() {
        return preparationSteps;
    }

    public ViewCount getViewCount() {
        return viewCount;
    }

    public Category getCategory() {
        return category;
    }

    public Recipe incrementViewCount() {
        return new Recipe(id, title, ingredients, preparationSteps, 
                         viewCount.increment(), category, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(id, recipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}