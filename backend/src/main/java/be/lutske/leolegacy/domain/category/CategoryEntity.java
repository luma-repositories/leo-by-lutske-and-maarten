package be.lutske.leolegacy.domain.category;

import java.util.Objects;

public class CategoryEntity {
    private String id;
    private String name;
    private String description;

    public CategoryEntity() {
    }

    public CategoryEntity(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category toDomain() {
        return new Category(new CategoryId(id), new CategoryName(name), description);
    }

    public void fromDomain(Category category) {
        this.id = category.getId().getValue();
        this.name = category.getName().getValue();
        this.description = category.getDescription();
    }
}