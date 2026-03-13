package be.lutske.leolegacy.domain.recipe;

import be.lutske.leolegacy.domain.category.CategoryId;
import be.lutske.leolegacy.domain.category.CategoryName;
import be.lutske.leolegacy.domain.viewcount.ViewCount;
import java.time.Instant;

public class Recipe {
    private final RecipeTitle title;
    private final Ingredients ingredients;
    private final Preparation preparation;
    private ViewCount viewCount;

    public Recipe(RecipeTitle title, Ingredients ingredients, Preparation preparation, ViewCount viewCount) {
        this.title = title;
        this.ingredients = ingredients;
        this.preparation = preparation;
        this.viewCount = viewCount;
    }

    // existing getters and methods




    public RecipeTitle getTitle() {
        return title;
    }

    public Ingredients getIngredients() {
        return ingredients;
    }

    public Preparation getPreparation() {
        return preparation;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public ViewCount getViewCount() {
        return viewCount;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getImportMetadata() {
        return importMetadata;
    }

    public void incrementViewCount() {
        this.viewCount = new ViewCount(this.viewCount.getValue() + 1);
    }
}