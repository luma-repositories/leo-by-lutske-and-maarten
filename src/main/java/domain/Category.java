package domain;

import java.util.UUID;

public class Category {
    private final String id;
    private final CategoryName name;
    private final ViewCount viewCount;

    public Category(CategoryName name, ViewCount viewCount) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.viewCount = viewCount;
    }

    public String getId() {
        return id;
    }

    public CategoryName getName() {
        return name;
    }

    public ViewCount getViewCount() {
        return viewCount;
    }

    public Category incrementViewCount() {
        return new Category(name, viewCount.increment());
    }

    @Override
    public String toString() {
        return "Category{id='" + id + "', name='" + name + "', viewCount='" + viewCount + "'";
    }
}