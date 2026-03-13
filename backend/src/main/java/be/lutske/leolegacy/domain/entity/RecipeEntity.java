package be.lutske.leolegacy.domain.entity;

import java.time.Instant;

public class RecipeEntity {
    private String title;
    private String ingredients;
    private String preparation;
    private int viewCount;
    private Long id;
    private CategoryEntity category;
    private String source;
    private Instant createdAt;
    private String importMetadata;

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public RecipeEntity(String title, String ingredients, String preparation, int viewCount, Long id, CategoryEntity category, String source, Instant createdAt, String importMetadata) {
        this.title = title;
        this.ingredients = ingredients;
        this.preparation = preparation;
        this.viewCount = viewCount;
        this.id = id;
        this.category = category;
        this.source = source;
        this.createdAt = createdAt;
        this.importMetadata = importMetadata;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getImportMetadata() {
        return importMetadata;
    }

    public void setImportMetadata(String importMetadata) {
        this.importMetadata = importMetadata;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}