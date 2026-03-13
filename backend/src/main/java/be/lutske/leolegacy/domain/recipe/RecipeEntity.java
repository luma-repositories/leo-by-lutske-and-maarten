package be.lutske.leolegacy.domain.recipe;

import be.lutske.leolegacy.domain.category.CategoryId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RecipeEntity {
    @Id
    private String id;
    private String title;
    private String description;
    private String categoryId;

    public RecipeEntity() {
    }

    public RecipeEntity(String id, String title, String description, String categoryId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Recipe toDomain() {
        return new Recipe(new RecipeId(id), title, description, new CategoryId(categoryId));
    }

    public void fromDomain(Recipe recipe) {
        this.id = recipe.getId().getValue();
        this.title = recipe.getTitle();
        this.description = recipe.getDescription();
        this.categoryId = recipe.getCategoryId().getValue();
    }
}