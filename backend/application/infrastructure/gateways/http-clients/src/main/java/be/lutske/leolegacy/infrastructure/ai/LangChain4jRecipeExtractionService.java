package be.lutske.leolegacy.infrastructure.ai;

import be.lutske.leolegacy.domain.recipe.RecipeExtraction;
import be.lutske.leolegacy.domain.recipe.RecipeExtractionException;
import be.lutske.leolegacy.port.out.RecipeImageExtractionPort;
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

@ApplicationScoped
public class LangChain4jRecipeExtractionService implements RecipeImageExtractionPort {

    private static final Logger LOG = Logger.getLogger(LangChain4jRecipeExtractionService.class);

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
    public RecipeExtraction extractRecipeFromImage(byte[] imageBytes, String mimeType) {
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

    private String callModel(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        var userMessage = UserMessage.from(
                ImageContent.from(base64Image, mimeType),
                TextContent.from(EXTRACTION_PROMPT));
        var response = chatModel.chat(userMessage);
        return response.aiMessage().text();
    }

    private RecipeExtraction parseModelResponse(String rawResponse) {
        String cleanedJson = CODE_FENCE_START.matcher(rawResponse).replaceAll("");
        cleanedJson = CODE_FENCE_END.matcher(cleanedJson).replaceAll("").trim();

        try {
            LlmRecipeResponse parsed = objectMapper.readValue(cleanedJson, LlmRecipeResponse.class);

            return new RecipeExtraction(
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
            return new RecipeExtraction(
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
