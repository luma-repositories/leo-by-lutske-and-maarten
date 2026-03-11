package be.lutske.leolegacy.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Domain model for a recipe. Framework-agnostic — no JPA, no JAX-RS.
 */
public final class Recipe {

    private Long id;
    private String title;
    private List<String> ingredients;
    private String preparation;
    private Category category;
    private int viewCount;
    private String source;
    private Instant createdAt;
    private String importMetadata;

    public Recipe() {
    }

    public Recipe(Long id, String title, List<String> ingredients, String preparation,
                  Category category, int viewCount, String source, Instant createdAt,
                  String importMetadata) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.preparation = preparation;
        this.category = category;
        this.viewCount = viewCount;
        this.source = source;
        this.createdAt = createdAt;
        this.importMetadata = importMetadata;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getPreparation() { return preparation; }
    public void setPreparation(String preparation) { this.preparation = preparation; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getImportMetadata() { return importMetadata; }
    public void setImportMetadata(String importMetadata) { this.importMetadata = importMetadata; }
}
