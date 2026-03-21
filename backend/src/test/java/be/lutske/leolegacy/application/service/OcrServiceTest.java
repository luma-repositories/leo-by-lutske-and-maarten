package be.lutske.leolegacy.application.service;

import net.sourceforge.tess4j.Tesseract;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class OcrServiceTest {

    @Test
    void defaultUsesItalianThenFallsBackToEnglishWhenNotMeaningful() {
        Map<String, String> responses = Map.of(
                "ita", "",
                "eng", "the recipe uses butter"
        );
        var created = new ArrayList<RecordingTesseract>();
        var service = buildService(responses, Optional.empty(), "./tessdata", created);

        String result = service.extractText(dummyImageBytes());

        assertEquals("the recipe uses butter", result);
        assertIterableEquals(List.of("ita", "eng"), attemptedLanguages(created));
    }

    @Test
    void fallsBackToDutchWhenItalianAndEnglishProvideNoHints() {
        Map<String, String> responses = Map.of(
                "ita", "",
                "eng", "zxqv",
                "nld", "het recept met kaas"
        );
        var created = new ArrayList<RecordingTesseract>();
        var service = buildService(responses, Optional.empty(), "./tessdata", created);

        String result = service.extractText(dummyImageBytes());

        assertEquals("het recept met kaas", result);
        assertIterableEquals(List.of("ita", "eng", "nld"), attemptedLanguages(created));
    }

    @Test
    void customLanguageOverrideBypassesFallback() {
        Map<String, String> responses = Map.of("eng", "custom english text");
        var created = new ArrayList<RecordingTesseract>();
        var service = buildService(responses, Optional.of("eng"), "./tessdata", created);

        String result = service.extractText(dummyImageBytes());

        assertEquals("custom english text", result);
        assertIterableEquals(List.of("eng"), attemptedLanguages(created));
    }

    @Test
    void compositeLanguageOverrideBypassesFallback() {
        Map<String, String> responses = Map.of("ita+eng", "combined text");
        var created = new ArrayList<RecordingTesseract>();
        var service = buildService(responses, Optional.of("ita+eng"), "./tessdata", created);

        String result = service.extractText(dummyImageBytes());

        assertEquals("combined text", result);
        assertIterableEquals(List.of("ita+eng"), attemptedLanguages(created));
    }

    @Test
    void tessdataPathComesFromConfiguration() {
        String customPath = "custom/path";
        Map<String, String> responses = Map.of("eng", "text");
        var created = new ArrayList<RecordingTesseract>();
        var service = buildService(responses, Optional.of("eng"), customPath, created);

        service.extractText(dummyImageBytes());

        assertEquals(customPath, created.getFirst().getDatapath());
    }

    private static OcrService buildService(Map<String, String> responses,
                                           Optional<String> languageOpt,
                                           String tessdataPath,
                                           List<RecordingTesseract> created) {
        Supplier<Tesseract> supplier = () -> {
            var stub = new RecordingTesseract(responses);
            created.add(stub);
            return stub;
        };
        return new OcrService(tessdataPath, languageOpt, supplier);
    }

    private static List<String> attemptedLanguages(List<RecordingTesseract> stubs) {
        return stubs.stream().map(RecordingTesseract::getLanguage).toList();
    }

    private static byte[] dummyImageBytes() {
        try {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            var baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build dummy image", e);
        }
    }

    private static final class RecordingTesseract extends Tesseract {
        private final Map<String, String> responses;
        private String language;
        private String datapath;

        RecordingTesseract(Map<String, String> responses) {
            this.responses = responses;
        }

        @Override
        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public void setDatapath(String datapath) {
            this.datapath = datapath;
        }

        @Override
        public String doOCR(BufferedImage bi) {
            return responses.getOrDefault(language, "");
        }

        String getLanguage() {
            return language;
        }

        String getDatapath() {
            return datapath;
        }
    }
}
