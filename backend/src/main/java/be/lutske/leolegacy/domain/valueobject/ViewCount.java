package be.lutske.leolegacy.domain.valueobject;

import java.util.Objects;

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

    public ViewCount increment() {
        return new ViewCount(value + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewCount that = (ViewCount) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ViewCount{" +
                "value=" + value +
                "}";
    }
}