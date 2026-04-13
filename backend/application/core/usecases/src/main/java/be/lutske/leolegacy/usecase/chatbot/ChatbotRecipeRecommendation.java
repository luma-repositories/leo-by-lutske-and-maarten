package be.lutske.leolegacy.usecase.chatbot;

public record ChatbotRecipeRecommendation(
        long recipeId,
        String title,
        String categoryName,
        String matchReason
) {
}
