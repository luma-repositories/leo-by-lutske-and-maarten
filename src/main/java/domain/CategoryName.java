package domain;

public record CategoryName(String value) {
    public CategoryName {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be null or empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Category name must not exceed 100 characters");
        }
    }
}