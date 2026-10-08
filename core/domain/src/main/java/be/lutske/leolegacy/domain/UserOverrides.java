package be.lutske.leolegacy.domain;

import java.util.List;

public record UserOverrides(String title, List<String> ingredients, String preparation,
                           Long categoryId, String notes, String servings, String description) {
}