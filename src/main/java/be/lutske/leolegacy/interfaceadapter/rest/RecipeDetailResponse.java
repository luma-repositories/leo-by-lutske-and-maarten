package be.lutske.leolegacy.interfaceadapter.rest;

public record RecipeDetailResponse(
        Long id,
        Integer legacyId,
        String title,
        String category,
        String ingredients,
        String preparation,
        String pdfSlug
) {
}
