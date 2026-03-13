package be.lutske.leolegacy.interfaceadapter.rest.dto;

import java.util.List;

public record RecipeDetailResponse(
    Long id,
    String title,
    String description,
    List<String> ingredients,
    String instructions,
    Long categoryId,
    String categoryName
) {
    // DTO for detailed recipe view
