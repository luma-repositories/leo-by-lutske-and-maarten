package be.lutske.leolegacy.domain.entity;

import java.util.Objects;

public class CategoryEntity {
    private String name;
    private Long id;

    public CategoryEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // existing getters and setters


    // existing getters and setters


}
}