package be.lutske.leolegacy.interfaceadapter.rest;

public record CategoryResponse(Long id, String slug, String name, long recipeCount) {
}
