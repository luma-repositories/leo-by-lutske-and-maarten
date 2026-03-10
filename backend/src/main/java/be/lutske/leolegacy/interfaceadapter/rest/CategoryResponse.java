package be.lutske.leolegacy.interfaceadapter.rest;

/**
 * JSON response DTO for a recipe category.
 */
public record CategoryResponse(
        long id,
        String name,
        long recipeCount
) {
}
