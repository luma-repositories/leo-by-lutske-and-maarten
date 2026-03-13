package be.lutske.leolegacy.interfaceadapter.rest.dto;

public record CategoryResponse(
    Long id,
    String name,
    String description
) {
    // DTO for category view
