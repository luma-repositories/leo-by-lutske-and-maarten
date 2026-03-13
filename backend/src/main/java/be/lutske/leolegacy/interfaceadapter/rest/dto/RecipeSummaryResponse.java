package be.lutske.leolegacy.interfaceadapter.rest.dto;

public record RecipeSummaryResponse(
    Long id,
    String title,
    String description,
    Long categoryId,
    String categoryName
) {
    // DTO for recipe summary view
