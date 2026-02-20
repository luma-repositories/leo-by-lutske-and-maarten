package be.lutske.leolegacy.domain;

public record RecipeDetail(
        Long id,
        Integer legacyId,
        String title,
        String category,
        String ingredients,
        String preparation,
        String pdfSlug
) {
}
