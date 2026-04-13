package be.lutske.leolegacy.entrypoint.rest;

import java.util.List;

public final class ChatbotDtos {

    private ChatbotDtos() {
    }

    public record ChatRequest(String message, List<ChatMessageContextDto> context) {
    }

    public record ChatResponse(String author, String message, List<RecipeRecommendationDto> recommendations) {
    }

    public record RecipeRecommendationDto(long recipeId, String title, String categoryName, String matchReason) {
    }

    public record ChatMessageContextDto(String role, String message) {
    }
}
