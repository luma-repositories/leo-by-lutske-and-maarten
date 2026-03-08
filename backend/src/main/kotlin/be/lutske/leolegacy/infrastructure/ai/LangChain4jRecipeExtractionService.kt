package be.lutske.leolegacy.infrastructure.ai

import be.lutske.leolegacy.application.service.ExtractionResult
import be.lutske.leolegacy.application.service.RecipeExtractionException
import be.lutske.leolegacy.application.service.RecipeExtractionService
import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Base64

/**
 * LangChain4j-based implementation of [RecipeExtractionService].
 *
 * Sends the uploaded image to a multimodal LLM via LangChain4j and parses
 * the structured JSON response into an [ExtractionResult].
 *
 * Provider-specific differences (image encoding, API format) are handled
 * internally — callers only see the provider-agnostic interface.
 */
@ApplicationScoped
class LangChain4jRecipeExtractionService(
    private val chatModel: ChatLanguageModel,
    private val objectMapper: ObjectMapper,

    @ConfigProperty(name = "app.ai.provider", defaultValue = "openai")
    private val provider: String,

    @ConfigProperty(name = "app.ai.model", defaultValue = "gpt-4o")
    private val modelName: String
) : RecipeExtractionService {

    companion object {
        private val LOG = Logger.getLogger(LangChain4jRecipeExtractionService::class.java)

        /**
         * Extraction prompt that instructs the model to return strict JSON.
         * Designed to minimize hallucination and maximize faithful extraction.
         */
        private val EXTRACTION_PROMPT = """
            You are a recipe extraction assistant. You receive an image of a recipe
            (handwritten, printed, or from a cookbook/website).

            Your task:
            1. Extract the recipe data from the image as accurately as possible.
            2. TRANSLATE ALL extracted content into English. The recipe in the image may be
               in any language (Dutch, Italian, French, etc.) — you MUST translate the title,
               ingredients, steps, description, and all other text fields into English.
            3. Return ONLY a valid JSON object — no markdown, no code fences, no explanation.
            4. Preserve ingredient quantities and units faithfully (translate unit names to English).
            5. If a field is not visible, unclear, or you are uncertain, set it to null.
            6. Do NOT invent or hallucinate data that is not in the image.
            7. Add warnings for any uncertainty or ambiguity.

            Required JSON structure:
            {
              "title": "string or null — in English",
              "description": "string or null — brief summary in English",
              "servings": "string or null — e.g. '4 servings'",
              "ingredients": ["string", ...] or null — each ingredient as a single string with amount+unit+item, in English,
              "steps": ["string", ...] or null — each preparation step as a string, in English,
              "source": "string or null — attribution if visible (keep original name)",
              "tags": ["string", ...] or null — categories or tags in English,
              "warnings": ["string", ...] — list of warnings about uncertain/missing data, in English
            }

            Important rules:
            - ALWAYS output in English, even if the source recipe is in another language.
            - If the image is not a recipe, return: {"title": null, "warnings": ["Image does not appear to contain a recipe"]}
            - If text is partially illegible, extract what you can and add a warning.
            - Ingredients should each be a single string like "200 g dark chocolate" or "3 eggs".
            - Steps should be individual instructions, not one big block of text.
        """.trimIndent()
    }

    override fun extractRecipeFromImage(imageBytes: ByteArray, mimeType: String): ExtractionResult {
        LOG.info("Extracting recipe from image using provider=$provider, model=$modelName, mimeType=$mimeType, size=${imageBytes.size} bytes")

        val rawResponse: String = try {
            callModel(imageBytes, mimeType)
        } catch (e: Exception) {
            LOG.error("LLM call failed: provider=$provider, model=$modelName", e)
            throw RecipeExtractionException(
                "Recipe extraction failed: provider=$provider, model=$modelName — ${e.message}",
                e
            )
        }

        LOG.debug("Raw model response length: ${rawResponse.length} chars")

        return parseModelResponse(rawResponse)
    }

    /**
     * Send the image to the LLM and get the raw text response.
     */
    private fun callModel(imageBytes: ByteArray, mimeType: String): String {
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)

        val userMessage = UserMessage.from(
            ImageContent.from(base64Image, mimeType),
            TextContent.from(EXTRACTION_PROMPT)
        )

        val response = chatModel.chat(userMessage)
        return response.aiMessage().text()
    }

    /**
     * Parse the raw model response (expected JSON) into an [ExtractionResult].
     * Handles malformed JSON defensively.
     */
    private fun parseModelResponse(rawResponse: String): ExtractionResult {
        // Strip markdown code fences if the model wrapped the JSON
        val cleanedJson = rawResponse
            .replace(Regex("^```json\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("^```\\s*", RegexOption.MULTILINE), "")
            .trim()

        return try {
            val parsed: LlmRecipeResponse = objectMapper.readValue(cleanedJson, LlmRecipeResponse::class.java)

            ExtractionResult(
                title = parsed.title?.takeIf { it.isNotBlank() },
                description = parsed.description?.takeIf { it.isNotBlank() },
                servings = parsed.servings?.takeIf { it.isNotBlank() },
                ingredients = parsed.ingredients?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() },
                steps = parsed.steps?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() },
                source = parsed.source?.takeIf { it.isNotBlank() },
                tags = parsed.tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() },
                warnings = parsed.warnings?.filter { it.isNotBlank() } ?: emptyList(),
                rawModelResponse = cleanedJson,
                provider = provider,
                model = modelName
            )
        } catch (e: Exception) {
            LOG.warn("Failed to parse model response as JSON, returning raw response with warnings", e)

            // Fallback: return the raw text as a warning so the user can manually extract
            ExtractionResult(
                warnings = listOf(
                    "Model response could not be parsed as structured JSON. " +
                    "Please review the raw response and fill in the recipe fields manually."
                ),
                rawModelResponse = cleanedJson,
                provider = provider,
                model = modelName
            )
        }
    }

    /**
     * Internal DTO matching the JSON structure we ask the LLM to produce.
     * All fields are nullable for defensive parsing.
     */
    data class LlmRecipeResponse(
        val title: String? = null,
        val description: String? = null,
        val servings: String? = null,
        val ingredients: List<String>? = null,
        val steps: List<String>? = null,
        val source: String? = null,
        val tags: List<String>? = null,
        val warnings: List<String>? = null
    )
}
