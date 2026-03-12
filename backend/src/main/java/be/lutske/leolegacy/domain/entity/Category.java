package be.lutske.leolegacy.domain.entity;

import be.lutske.leolegacy.domain.valueobject.CategoryId;
import be.lutske.leolegacy.domain.valueobject.CategoryName;

import java.util.Objects;

public class Category {
    private final CategoryId id;
    private final CategoryName name;

    // Constructor for creating new categories
    public Category(CategoryId id, CategoryName name) {
        this.id = id;
        this.name = name;
    }

    // Constructor for reconstituting existing categories
    public Category(CategoryId id, CategoryName name, boolean reconstitute) {
        this.id = id;
        this.name = name;
    }

    public CategoryId getId() {
        return id;
    }

    public CategoryName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}