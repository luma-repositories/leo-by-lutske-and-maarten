package be.lutske.leolegacy.ports.gateway;

import be.lutske.leolegacy.domain.AiExtractionResult;
import java.util.Optional;

/**
 * Port Interface for AI extraction services. Acts as a gateway, isolating the Use Case
 * from concrete SDK implementations (OpenAI, Anthropic, etc.).
 */
public interface AiExtractionGateway {

    /** 
     * Reads the input (e.g., image, text) and attempts to extract structured recipe data.
     * @param inputSource The source of the data (e.g., file path, image byte array, raw text).
     * @return An Optional containing the structured result if extraction was successful.
     * @throws IllegalStateException if the service fails critically (e.g., API key missing).
     */
    Optional<AiExtractionResult> extractRecipe(String inputSource);
}