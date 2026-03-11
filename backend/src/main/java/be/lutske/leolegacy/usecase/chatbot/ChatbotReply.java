package be.lutske.leolegacy.usecase.chatbot;

import java.util.List;

public record ChatbotReply(
        String author,
        String message,
        List<ChatbotRecipeRecommendation> recommendations
) {
    public ChatbotReply {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
}
