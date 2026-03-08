package be.lutske.leolegacy.infrastructure.ai

import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration
import java.util.Optional

/**
 * CDI producer that creates the correct [ChatLanguageModel] based on configuration.
 *
 * Supported providers:
 * - `openai`  — OpenAI API (GPT-4o, etc.)
 * - `claude`  — Anthropic Claude API
 * - `vllm`    — Local vLLM with OpenAI-compatible API (uses OpenAI client with custom base URL)
 *
 * The provider is selected via `app.ai.provider` in application.properties.
 * All providers share the same config keys where applicable.
 */
@ApplicationScoped
class ChatModelProducer(
    @ConfigProperty(name = "app.ai.provider", defaultValue = "openai")
    private val provider: String,

    @ConfigProperty(name = "app.ai.model", defaultValue = "gpt-4o")
    private val modelName: String,

    @ConfigProperty(name = "app.ai.api-key", defaultValue = "dummy-key-for-tests")
    private val apiKey: String,

    @ConfigProperty(name = "app.ai.base-url")
    private val baseUrl: Optional<String>,

    @ConfigProperty(name = "app.ai.timeout-seconds", defaultValue = "120")
    private val timeoutSeconds: Long,

    @ConfigProperty(name = "app.ai.temperature", defaultValue = "0.1")
    private val temperature: Double,

    @ConfigProperty(name = "app.ai.max-tokens", defaultValue = "4096")
    private val maxTokens: Int
) {
    companion object {
        private val LOG = Logger.getLogger(ChatModelProducer::class.java)
    }

    @Produces
    @ApplicationScoped
    fun chatLanguageModel(): ChatLanguageModel {
        LOG.info("Creating ChatLanguageModel: provider=$provider, model=$modelName")

        return when (provider.lowercase()) {
            "openai" -> buildOpenAiModel(
                apiKey = apiKey,
                modelName = modelName,
                baseUrl = null // use default OpenAI endpoint
            )

            "vllm" -> {
                val url = baseUrl.orElseThrow {
                    IllegalStateException(
                        "app.ai.base-url is required when app.ai.provider=vllm. " +
                        "Set it to your vLLM endpoint, e.g. http://localhost:8000/v1"
                    )
                }
                LOG.info("Using vLLM endpoint: $url (model must be vision-capable)")
                buildOpenAiModel(
                    apiKey = apiKey,
                    modelName = modelName,
                    baseUrl = url
                )
            }

            "claude" -> buildAnthropicModel()

            else -> throw IllegalStateException(
                "Unsupported AI provider: '$provider'. Supported: openai, claude, vllm"
            )
        }
    }

    private fun buildOpenAiModel(apiKey: String, modelName: String, baseUrl: String?): ChatLanguageModel {
        val builder = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)

        if (baseUrl != null) {
            builder.baseUrl(baseUrl)
        }

        return builder.build()
    }

    private fun buildAnthropicModel(): ChatLanguageModel {
        return AnthropicChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)
            .build()
    }
}
