package be.lutske.leolegacy.domain;

public record Category(Long id, String slug, String name, long recipeCount) {
}
