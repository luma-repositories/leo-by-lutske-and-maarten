package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.usecase.chatbot.ChatbotReply;

final class RestChatbotMapper {

    private RestChatbotMapper() {
    }

    static ChatbotDtos.ChatResponse toResponse(ChatbotReply reply) {
        return new ChatbotDtos.ChatResponse(
                reply.author(),
                reply.message(),
                reply.recommendations().stream()
                        .map(recommendation -> new ChatbotDtos.RecipeRecommendationDto(
                                recommendation.recipeId(),
                                recommendation.title(),
                                recommendation.categoryName(),
                                recommendation.matchReason()
                        ))
                        .toList()
        );
    }
}
