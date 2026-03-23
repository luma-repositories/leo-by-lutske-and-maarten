package be.lutske.leolegacy.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.sourceforge.tess4j.Tesseract;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.System.setProperty;

/**
 * Service that wraps Tess4J to perform OCR on uploaded images with simple language fallback.
 *
 * Fallback applies only when no explicit language is configured (property absent):
 * 1) Italian (ita) → 2) English (eng) → 3) Dutch (nld)
 * Custom language values (e.g. "ita+eng", "eng", "nld") bypass the fallback.
 */
@ApplicationScoped
public class OcrService {

    private static final String MACOS_HOMEBREW_LIB_PATH = "/opt/homebrew/lib";
    private static final String LINUX_LIB_PATH = "/usr/local/lib";

    private static void configureJnaLibraryPath() {
        if (System.getProperty("jna.library.path") == null) {
            File homebrewLib = new File(MACOS_HOMEBREW_LIB_PATH);
            File linuxLib = new File(LINUX_LIB_PATH);
            if (homebrewLib.exists()) {
                setProperty("jna.library.path", MACOS_HOMEBREW_LIB_PATH);
            } else if (linuxLib.exists()) {
                setProperty("jna.library.path", LINUX_LIB_PATH);
            }
        }
    }

    static {
        configureJnaLibraryPath();
    }

    private static final Map<String, Set<String>> LANGUAGE_HINTS = Map.of(
            "ita", Set.of("il", "la", "di", "che", "per", "con", "una", "un", "gli",
                    "ingredienti", "procedimento", "preparazione", "ricetta"),
            "eng", Set.of("the", "and", "with", "ingredients", "directions", "method", "steps",
                    "recipe", "mix", "bake"),
            "nld", Set.of("de", "het", "en", "voor", "bereiding", "werkwijze", "ingredienten",
                    "ingrediënten", "recept")
    );

    private final String tessdataPath;
    private final String configuredLanguage;
    private final boolean languageProvided;
    private final Supplier<Tesseract> tesseractSupplier;

    @Inject
    public OcrService(
            @ConfigProperty(name = "ocr.tessdata-path", defaultValue = "./tessdata") String tessdataPath,
            @ConfigProperty(name = "ocr.language") Optional<String> languageOpt
    ) {
        this(tessdataPath, languageOpt, Tesseract::new);
    }

    OcrService(String tessdataPath, Optional<String> languageOpt, Supplier<Tesseract> tesseractSupplier) {
        this.tessdataPath = tessdataPath;
        this.configuredLanguage = languageOpt.orElse("ita");
        this.languageProvided = languageOpt.isPresent();
        this.tesseractSupplier = tesseractSupplier;
    }

    /**
     * Extract text from image bytes using Tesseract OCR.
     *
     * @param imageBytes the raw image file bytes (PNG, JPG, WEBP)
     * @return the extracted text
     * @throws OcrException if OCR fails
     */
    public String extractText(byte[] imageBytes) {
        BufferedImage image = readImage(imageBytes);

        if (shouldBypassFallback(configuredLanguage, languageProvided)) {
            return runOcr(image, configuredLanguage);
        }

        List<String> fallbackLanguages = List.of("ita", "eng", "nld");
        String lastResult = "";
        Exception lastError = null;

        for (String lang : fallbackLanguages) {
            try {
                String text = runOcr(image, lang);
                lastResult = text;
                if (isMeaningful(text, lang)) {
                    return text;
                }
            } catch (Exception e) {
                lastError = e;
            }
        }

        if (lastError != null && (lastResult == null || lastResult.isBlank())) {
            throw new OcrException("Tesseract OCR failed: " + lastError.getMessage(), lastError);
        }
        return lastResult;
    }

    private BufferedImage readImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new OcrException("Could not read image — unsupported format or corrupt file");
            }
            return image;
        } catch (OcrException e) {
            throw e;
        } catch (Exception e) {
            throw new OcrException("Failed to read image: " + e.getMessage(), e);
        }
    }

    private String runOcr(BufferedImage image, String language) {
        try {
            var tesseract = tesseractSupplier.get();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(language);
            return tesseract.doOCR(image);
        } catch (Exception e) {
            throw new OcrException("Tesseract OCR failed: " + e.getMessage(), e);
        }
    }

    private boolean isMeaningful(String text, String language) {
        if (text == null) {
            return false;
        }
        String normalized = text.toLowerCase().replaceAll("[^\\p{L}\\s]", " ");
        List<String> tokens = Arrays.stream(normalized.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
        if (tokens.isEmpty()) {
            return false;
        }
        Set<String> hints = LANGUAGE_HINTS.get(language);
        if (hints == null) {
            return tokens.size() >= 3;
        }
        return tokens.stream().anyMatch(hints::contains);
    }

    private boolean shouldBypassFallback(String language, boolean provided) {
        if (provided) {
            return true;
        }
        if (language == null || language.isBlank()) {
            return false;
        }
        // Only use fallback when language is not explicitly configured and we're on the default "ita"
        return !language.equalsIgnoreCase("ita");
    }
}
