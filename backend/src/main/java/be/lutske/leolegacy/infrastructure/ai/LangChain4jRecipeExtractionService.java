package be.lutske.leolegacy.infrastructure.ai;

import be.lutske.leolegacy.application.service.ExtractionResult;
import be.lutske.leolegacy.application.service.RecipeExtractionException;
import be.lutske.leolegacy.application.service.RecipeExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * LangChain4j-based implementation of {@link RecipeExtractionService}.
 *
 * Sends the uploaded image to a multimodal LLM via LangChain4j and parses
 * the structured JSON response into an {@link ExtractionResult}.
 *
 * Provider-specific differences (image encoding, API format) are handled
 * internally — callers only see the provider-agnostic interface.
 */
@ApplicationScoped
public class LangChain4jRecipeExtractionService implements RecipeExtractionService {

    private static final Logger LOG = Logger.getLogger(LangChain4jRecipeExtractionService.class);

    /**
     * Extraction prompt that instructs the model to return strict JSON.
     * Designed to minimize hallucination and maximize faithful extraction.
     */
    private static final String EXTRACTION_PROMPT = """
            You are a recipe extraction assistant. You receive an image of a recipe
            (handwritten, printed, or from a cookbook/website).

            Your task:
            1. Extract the recipe data from the image as accurately as possible.
            2. Return ONLY a valid JSON object — no markdown, no code fences, no explanation.
            3. Preserve ingredient quantities, units, and cooking steps faithfully.
            4. If a field is not visible, unclear, or you are uncertain, set it to null.
            5. Do NOT invent or hallucinate data that is not in the image.
            6. Add warnings for any uncertainty or ambiguity.

            Required JSON structure:
            {
              "title": "string or null",
              "description": "string or null — brief summary if visible",
              "servings": "string or null — e.g. '4 servings'",
              "ingredients": ["string", ...] or null — each ingredient as a single string with amount+unit+item,
              "steps": ["string", ...] or null — each preparation step as a string,
              "source": "string or null — attribution if visible",
              "tags": ["string", ...] or null — categories or tags if visible,
              "warnings": ["string", ...] — list of warnings about uncertain/missing data
            }

            Important rules:
            - The recipe may be in any language. ALWAYS translate ALL extracted content to English, regardless of the original language.
            - If the image is not a recipe, return: {"title": null, "warnings": ["Image does not appear to contain a recipe"]}
            - If text is partially illegible, extract what you can and add a warning.
            - Ingredients should each be a single string like "200 g flour" or "3 eggs".
            - Steps should be individual instructions, not one big block of text.
            """;

    private static final Pattern CODE_FENCE_START = Pattern.compile("^```json\\s*", Pattern.MULTILINE);
    private static final Pattern CODE_FENCE_END = Pattern.compile("^```\\s*", Pattern.MULTILINE);

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final String provider;
    private final String modelName;

    public LangChain4jRecipeExtractionService(
            ChatLanguageModel chatModel,
            ObjectMapper objectMapper,
            @ConfigProperty(name = "app.ai.provider", defaultValue = "openai") String provider,
            @ConfigProperty(name = "app.ai.model", defaultValue = "gpt-4o") String modelName) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.modelName = modelName;
    }

    @Override
    public ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType) {
        LOG.infof("Extracting recipe from image using provider=%s, model=%s, mimeType=%s, size=%d bytes",
                provider, modelName, mimeType, imageBytes.length);

        String rawResponse;
        try {
            rawResponse = callModel(imageBytes, mimeType);
        } catch (Exception e) {
            LOG.errorf(e, "LLM call failed: provider=%s, model=%s", provider, modelName);
            throw new RecipeExtractionException(
                    "Recipe extraction failed: provider=" + provider + ", model=" + modelName + " — " + e.getMessage(),
                    e);
        }

        LOG.debugf("Raw model response length: %d chars", rawResponse.length());

        return parseModelResponse(rawResponse);
    }

    /**
     * Send the image to the LLM and get the raw text response.
     */
    private String callModel(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        var userMessage = UserMessage.from(
                ImageContent.from(base64Image, mimeType),
                TextContent.from(EXTRACTION_PROMPT));

        var response = chatModel.chat(userMessage);
        return response.aiMessage().text();
    }

    /**
     * Parse the raw model response (expected JSON) into an {@link ExtractionResult}.
     * Handles malformed JSON defensively.
     */
    private ExtractionResult parseModelResponse(String rawResponse) {
        // Strip markdown code fences if the model wrapped the JSON
        String cleanedJson = CODE_FENCE_START.matcher(rawResponse).replaceAll("");
        cleanedJson = CODE_FENCE_END.matcher(cleanedJson).replaceAll("").trim();

        try {
            LlmRecipeResponse parsed = objectMapper.readValue(cleanedJson, LlmRecipeResponse.class);

            return new ExtractionResult(
                    blankToNull(parsed.title()),
                    blankToNull(parsed.description()),
                    blankToNull(parsed.servings()),
                    filterBlanks(parsed.ingredients()),
                    filterBlanks(parsed.steps()),
                    blankToNull(parsed.source()),
                    filterBlanks(parsed.tags()),
                    parsed.warnings() != null
                            ? parsed.warnings().stream().filter(w -> w != null && !w.isBlank()).toList()
                            : List.of(),
                    cleanedJson,
                    provider,
                    modelName);
        } catch (Exception e) {
            LOG.warn("Failed to parse model response as JSON, returning raw response with warnings", e);

            // Fallback: return the raw text as a warning so the user can manually extract
            return new ExtractionResult(
                    null, null, null, null, null, null, null,
                    List.of("Model response could not be parsed as structured JSON. " +
                            "Please review the raw response and fill in the recipe fields manually."),
                    cleanedJson,
                    provider,
                    modelName);
        }
    }

    private static String blankToNull(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }

    private static List<String> filterBlanks(List<String> list) {
        if (list == null) return null;
        var filtered = list.stream().filter(s -> s != null && !s.isBlank()).toList();
        return filtered.isEmpty() ? null : filtered;
    }

    /**
     * Internal DTO matching the JSON structure we ask the LLM to produce.
     * All fields are nullable for defensive parsing.
     */
    record LlmRecipeResponse(
            String title,
            String description,
            String servings,
            List<String> ingredients,
            List<String> steps,
            String source,
            List<String> tags,
            List<String> warnings
    ) {}
}
