package be.lutske.leolegacy.domain.viewcount;

public class ViewCount {
    private final int value;

    public ViewCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("View count cannot be negative");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewCount viewCount = (ViewCount) o;
        return value == viewCount.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}