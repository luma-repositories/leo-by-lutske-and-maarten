package be.lutske.leolegacy.infrastructure.ocr;

import be.lutske.leolegacy.application.port.OcrPort;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.eclipse.microprofile.config.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@ApplicationScoped
public class Tess4jOcrAdapter implements OcrPort {

    private final String language;
    private final String tessdataPath;
    private final String stubText;

    @Inject
    public Tess4jOcrAdapter(Config config) {
        this(
                config.getOptionalValue("app.import.ocr.language", String.class).orElse("eng"),
                config.getOptionalValue("app.import.ocr.tessdata-path", String.class).orElse(""),
                config.getOptionalValue("app.import.ocr.stub-text", String.class).orElse("")
        );
    }

    Tess4jOcrAdapter(String language, String tessdataPath, String stubText) {
        this.language = language;
        this.tessdataPath = tessdataPath;
        this.stubText = stubText;
    }

    @Override
    public String extractText(byte[] imageBytes) {
        if (!stubText.isBlank()) {
            return stubText;
        }

        BufferedImage image = readImage(imageBytes);
        BufferedImage grayImage = toGrayscale(image);

        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage(language);
        if (!tessdataPath.isBlank()) {
            tesseract.setDatapath(tessdataPath);
        }

        try {
            return doOcr(tesseract, grayImage);
        } catch (LinkageError e) {
            throw new IllegalStateException("OCR_RUNTIME_UNAVAILABLE", e);
        } catch (TesseractException e) {
            throw new IllegalStateException("OCR_FAILED", e);
        }
    }

    String doOcr(Tesseract tesseract, BufferedImage image) throws TesseractException {
        return tesseract.doOCR(image);
    }

    private BufferedImage readImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("Unsupported or invalid image content.");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read image.", e);
        }
    }

    private BufferedImage toGrayscale(BufferedImage source) {
        BufferedImage grayImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayImage.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return grayImage;
    }
}
