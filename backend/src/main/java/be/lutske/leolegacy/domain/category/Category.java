package be.lutske.leolegacy.domain.category;

import java.util.Objects;

public class Category {
    private final Long id;
    private final String name;
    private final String description;

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
        this.description = "";
    }

    public Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Other methods...
}

    public CategoryName getName() {
        return name;
    }

    public CategoryId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}