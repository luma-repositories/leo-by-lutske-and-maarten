package be.lutske.leolegacy.infrastructure.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tess4jOcrAdapterTest {

    @Test
    void shouldReturnStubTextWhenProvided() {
        Tess4jOcrAdapter adapter = new Tess4jOcrAdapter("eng", "", "Stubbed OCR text");

        String result = adapter.extractText(createPngBytes());

        assertEquals("Stubbed OCR text", result);
    }

    @Test
    void shouldMapMissingNativeLibraryToControlledError() {
        Tess4jOcrAdapter adapter = new LinkageFailingAdapter();

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> adapter.extractText(createPngBytes()));
        assertEquals("OCR_RUNTIME_UNAVAILABLE", error.getMessage());
    }

    private byte[] createPngBytes() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    private static class LinkageFailingAdapter extends Tess4jOcrAdapter {

        LinkageFailingAdapter() {
            super("eng", "", "");
        }

        @Override
        String doOcr(Tesseract tesseract, BufferedImage image) throws TesseractException {
            throw new UnsatisfiedLinkError("libtesseract missing");
        }
    }
}
