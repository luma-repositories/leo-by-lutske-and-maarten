package domain;

public record ViewCount(int value) {
    public ViewCount {
        if (value < 0) {
            throw new IllegalArgumentException("View count must be non-negative");
        }
    }

    public ViewCount increment() {
        return new ViewCount(value + 1);
    }
}