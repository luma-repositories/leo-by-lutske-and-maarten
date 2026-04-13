package be.lutske.leolegacy.usecase.chatbot;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.port.out.RecipeQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnswerRecipeChatMessageUseCase {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "an", "and", "any", "are", "based", "can", "dish", "dishes", "disch",
            "find", "for", "give", "hello", "hey", "i", "ideas", "in", "is", "like", "made", "me",
            "of", "on", "please", "recipe", "recipes", "show", "some", "something", "suggest", "tell",
            "that", "the", "there", "to", "want", "with", "would"
    );

    private static final Map<String, List<String>> TERM_SYNONYMS = Map.ofEntries(
            Map.entry("tomato", List.of("tomato", "tomatoes", "tomato sauce", "tomato paste", "ketchup")),
            Map.entry("chicken", List.of("chicken", "turkey", "drumsticks", "fillet")),
            Map.entry("pasta", List.of("pasta", "spaghetti", "ravioli", "macaroni", "tagliatelle", "lasagna", "pizza")),
            Map.entry("soup", List.of("soup", "soep", "broth")),
            Map.entry("dessert", List.of("dessert", "desserts", "mousse", "cake", "sweet")),
            Map.entry("beef", List.of("beef", "meat", "ground beef", "minced meat")),
            Map.entry("fish", List.of("fish", "salmon", "cod", "pollock", "seafood")),
            Map.entry("veggie", List.of("veggie", "vegetarian", "vegetable", "vegetables", "broccoli", "zucchini", "carrot", "tomato", "tomatoes", "mushroom", "mushrooms")),
            Map.entry("light", List.of("light", "fresh", "salad", "salads", "soup", "soups", "carpaccio", "vegetable")),
            Map.entry("spicy", List.of("spicy", "curry", "paprika", "cayenne", "pepper", "serbian", "mexican", "devilishly")),
            Map.entry("oven dish", List.of("oven dish", "oven", "bake", "baked", "gratin", "gratinated", "roasted"))
    );

    private static final int RECOMMENDATION_LIMIT = 5;
    private static final Set<String> REFINEMENT_MARKERS = Set.of("make", "more", "less", "instead", "lighter", "vegetarian", "veggie", "spicy", "mild");

    private final RecipeQueryPort recipeQueryPort;

    public AnswerRecipeChatMessageUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public ChatbotReply answer(String question, List<ChatMessageContext> context) {
        var normalizedQuestion = question == null ? "" : question.trim();
        var interpretedQuestion = interpretQuestion(normalizedQuestion, context == null ? List.of() : context);
        var searchTerms = interpretedQuestion.expandedTerms();

        if (searchTerms.isEmpty()) {
            return new ChatbotReply(
                    "Leonardo",
                    "Hello, I am Leonardo. Tell me which ingredients or flavors you feel like eating, and I will guide you to fitting recipes.",
                    List.of()
            );
        }

        var matches = recipeQueryPort.findBySearchTerms(searchTerms, RECOMMENDATION_LIMIT * 4).stream()
                .sorted(Comparator.comparingInt((Recipe recipe) -> scoreRecipe(recipe, interpretedQuestion)).reversed())
                .limit(RECOMMENDATION_LIMIT)
                .toList();
        if (matches.isEmpty()) {
            return new ChatbotReply(
                    "Leonardo",
                    "I am Leonardo. I could not find a recipe for that yet, but try asking for an ingredient, category, or flavor such as tomato, pasta, soup, or chicken.",
                    List.of()
            );
        }

        var recommendations = matches.stream()
                .map(recipe -> new ChatbotRecipeRecommendation(
                        recipe.id(),
                        recipe.title(),
                        recipe.category().name(),
                        describeMatch(recipe, interpretedQuestion)
                ))
                .toList();

        var joinedTerms = String.join(", ", interpretedQuestion.displayTerms());
        return new ChatbotReply(
                "Leonardo",
                "I am Leonardo. Based on your question about " + joinedTerms + ", here are a few recipes you can open right away.",
                recommendations
        );
    }

    private InterpretedQuestion interpretQuestion(String question, List<ChatMessageContext> context) {
        if (question.isBlank()) {
            return new InterpretedQuestion(List.of(), List.of());
        }

        String normalized = question.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isBlank()) {
            return new InterpretedQuestion(List.of(), List.of());
        }

        Set<String> displayTerms = new LinkedHashSet<>();
        Set<String> expandedTerms = new LinkedHashSet<>();
        for (String token : normalized.split(" ")) {
            if (token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }

            var normalizedToken = singularize(token);
            displayTerms.add(normalizedToken);
            expandedTerms.add(normalizedToken);
            expandedTerms.addAll(TERM_SYNONYMS.getOrDefault(normalizedToken, List.of()));
        }

        if (normalized.contains("oven dish")) {
            displayTerms.add("oven dish");
            expandedTerms.addAll(TERM_SYNONYMS.get("oven dish"));
        }

        if (shouldUseContext(normalized)) {
            var previousUserMessages = context.stream()
                    .filter(message -> "user".equalsIgnoreCase(message.role()))
                    .map(ChatMessageContext::message)
                    .filter(message -> message != null && !message.isBlank())
                    .toList();
            if (!previousUserMessages.isEmpty()) {
                var priorTerms = interpretWithoutContext(previousUserMessages.getLast());
                displayTerms.addAll(priorTerms.displayTerms());
                expandedTerms.addAll(priorTerms.expandedTerms());
            }
        }

        for (String displayTerm : List.copyOf(displayTerms)) {
            TERM_SYNONYMS.forEach((canonical, synonyms) -> {
                if (synonyms.contains(displayTerm)) {
                    displayTerms.add(canonical);
                    expandedTerms.add(canonical);
                    expandedTerms.addAll(synonyms);
                }
            });
        }

        return new InterpretedQuestion(List.copyOf(displayTerms), List.copyOf(expandedTerms));
    }

    private InterpretedQuestion interpretWithoutContext(String question) {
        return interpretQuestion(question, List.of());
    }

    private boolean shouldUseContext(String normalizedQuestion) {
        return normalizedQuestion.contains("make it") || REFINEMENT_MARKERS.stream().anyMatch(normalizedQuestion::contains);
    }

    private String singularize(String token) {
        if (token.endsWith("es") && token.length() > 4) {
            return token.substring(0, token.length() - 2);
        }
        if (token.endsWith("s") && token.length() > 3) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private String describeMatch(Recipe recipe, InterpretedQuestion interpretedQuestion) {
        String haystack = searchableText(recipe);
        Map<String, List<String>> matchesByDisplayTerm = new LinkedHashMap<>();
        for (String displayTerm : interpretedQuestion.displayTerms()) {
            List<String> candidateTerms = new ArrayList<>();
            candidateTerms.add(displayTerm);
            candidateTerms.addAll(TERM_SYNONYMS.getOrDefault(displayTerm, List.of()));

            List<String> matchedCandidates = candidateTerms.stream()
                    .map(candidate -> candidate.toLowerCase(Locale.ROOT))
                    .distinct()
                    .filter(haystack::contains)
                    .toList();

            if (!matchedCandidates.isEmpty()) {
                matchesByDisplayTerm.put(displayTerm, matchedCandidates);
            }
        }

        if (matchesByDisplayTerm.isEmpty()) {
            return "Matches your request.";
        }

        return "Matches: " + matchesByDisplayTerm.entrySet().stream()
                .map(entry -> formatMatch(entry.getKey(), entry.getValue()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("your request") + ".";
    }

    private String formatMatch(String displayTerm, List<String> matchedCandidates) {
        List<String> distinctMatches = matchedCandidates.stream().distinct().toList();
        if (distinctMatches.size() == 1 && distinctMatches.getFirst().equals(displayTerm)) {
            return displayTerm;
        }

        return displayTerm + " via " + String.join("/", distinctMatches);
    }

    private String searchableText(Recipe recipe) {
        return String.join(
                " ",
                recipe.title(),
                recipe.category().name(),
                recipe.preparation(),
                String.join(" ", recipe.ingredients())
        ).toLowerCase(Locale.ROOT);
    }

    private int scoreRecipe(Recipe recipe, InterpretedQuestion interpretedQuestion) {
        String title = recipe.title().toLowerCase(Locale.ROOT);
        String category = recipe.category().name().toLowerCase(Locale.ROOT);
        String preparation = recipe.preparation().toLowerCase(Locale.ROOT);
        String ingredients = String.join(" ", recipe.ingredients()).toLowerCase(Locale.ROOT);

        int score = recipe.viewCount();
        for (String displayTerm : interpretedQuestion.displayTerms()) {
            String normalizedDisplayTerm = displayTerm.toLowerCase(Locale.ROOT);
            List<String> synonymTerms = TERM_SYNONYMS.getOrDefault(displayTerm, List.of()).stream()
                    .map(term -> term.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

            if (title.contains(normalizedDisplayTerm)) {
                score += 120;
            }
            if (category.contains(normalizedDisplayTerm)) {
                score += 100;
            }
            if (ingredients.contains(normalizedDisplayTerm)) {
                score += 70;
            }
            if (preparation.contains(normalizedDisplayTerm)) {
                score += 50;
            }

            for (String synonym : synonymTerms) {
                if (synonym.equals(normalizedDisplayTerm)) {
                    continue;
                }
                if (title.contains(synonym)) {
                    score += 45;
                }
                if (category.contains(synonym)) {
                    score += 40;
                }
                if (ingredients.contains(synonym)) {
                    score += 28;
                }
                if (preparation.contains(synonym)) {
                    score += 20;
                }
            }
        }
        return score;
    }

    private record InterpretedQuestion(List<String> displayTerms, List<String> expandedTerms) {
    }
}
