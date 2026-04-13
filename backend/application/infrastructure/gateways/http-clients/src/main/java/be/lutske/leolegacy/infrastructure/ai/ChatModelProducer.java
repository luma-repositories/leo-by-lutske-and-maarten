package be.lutske.leolegacy.infrastructure.ai;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class ChatModelProducer {

    private static final Logger LOG = Logger.getLogger(ChatModelProducer.class);

    private final String provider;
    private final String modelName;
    private final String apiKey;
    private final Optional<String> baseUrl;
    private final long timeoutSeconds;
    private final double temperature;
    private final int maxTokens;

    public ChatModelProducer(
            @ConfigProperty(name = "app.ai.provider", defaultValue = "openai") String provider,
            @ConfigProperty(name = "app.ai.model", defaultValue = "gpt-4o") String modelName,
            @ConfigProperty(name = "app.ai.api-key", defaultValue = "dummy-key-for-tests") String apiKey,
            @ConfigProperty(name = "app.ai.base-url") Optional<String> baseUrl,
            @ConfigProperty(name = "app.ai.timeout-seconds", defaultValue = "120") long timeoutSeconds,
            @ConfigProperty(name = "app.ai.temperature", defaultValue = "0.1") double temperature,
            @ConfigProperty(name = "app.ai.max-tokens", defaultValue = "4096") int maxTokens) {
        this.provider = provider;
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    @Produces
    @ApplicationScoped
    public ChatLanguageModel chatLanguageModel() {
        LOG.infof("Creating ChatLanguageModel: provider=%s, model=%s", provider, modelName);

        return switch (provider.toLowerCase()) {
            case "openai" -> buildOpenAiModel(apiKey, modelName, null);
            case "vllm" -> {
                String url = baseUrl.orElseThrow(() ->
                        new IllegalStateException(
                                "app.ai.base-url is required when app.ai.provider=vllm. " +
                                "Set it to your vLLM endpoint, e.g. http://localhost:8000/v1"));
                LOG.infof("Using vLLM endpoint: %s (model must be vision-capable)", url);
                yield buildOpenAiModel(apiKey, modelName, url);
            }
            case "claude" -> buildAnthropicModel();
            default -> throw new IllegalStateException(
                    "Unsupported AI provider: '" + provider + "'. Supported: openai, claude, vllm");
        };
    }

    private ChatLanguageModel buildOpenAiModel(String apiKey, String modelName, String baseUrl) {
        var builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .logRequests(false)
                .logResponses(false);

        if (baseUrl != null) {
            builder.baseUrl(baseUrl);
        }

        return builder.build();
    }

    private ChatLanguageModel buildAnthropicModel() {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
