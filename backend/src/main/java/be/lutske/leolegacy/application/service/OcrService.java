package be.lutske.leolegacy.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import net.sourceforge.tess4j.Tesseract;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Service that wraps Tess4J to perform OCR on uploaded images.
 *
 * Requires Tesseract to be installed locally:
 *   macOS: brew install tesseract
 *   Linux: apt-get install tesseract-ocr
 *
 * The tessdata path and language are configurable via application.properties.
 */
@ApplicationScoped
public class OcrService {

    private final String tessdataPath;
    private final String language;

    public OcrService(
            @ConfigProperty(name = "ocr.tessdata-path", defaultValue = "/usr/local/share/tessdata") String tessdataPath,
            @ConfigProperty(name = "ocr.language", defaultValue = "eng") String language) {
        this.tessdataPath = tessdataPath;
        this.language = language;
    }

    /**
     * Extract text from image bytes using Tesseract OCR.
     *
     * @param imageBytes the raw image file bytes (PNG, JPG, WEBP)
     * @return the extracted text
     * @throws OcrException if OCR fails
     */
    public String extractText(byte[] imageBytes) {
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new OcrException("Could not read image — unsupported format or corrupt file");
            }
        } catch (OcrException e) {
            throw e;
        } catch (Exception e) {
            throw new OcrException("Failed to read image: " + e.getMessage(), e);
        }

        try {
            var tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(language);
            return tesseract.doOCR(image);
        } catch (Exception e) {
            throw new OcrException("Tesseract OCR failed: " + e.getMessage(), e);
        }
    }
}
