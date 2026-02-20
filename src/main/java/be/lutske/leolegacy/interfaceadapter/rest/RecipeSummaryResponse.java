package be.lutske.leolegacy.interfaceadapter.rest;

public record RecipeSummaryResponse(Long id, Integer legacyId, String title, String category, String excerpt) {
}
