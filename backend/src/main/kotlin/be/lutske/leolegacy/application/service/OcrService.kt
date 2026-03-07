package be.lutske.leolegacy.application.service

import jakarta.enterprise.context.ApplicationScoped
import net.sourceforge.tess4j.Tesseract
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

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
class OcrService(
    @ConfigProperty(name = "ocr.tessdata-path", defaultValue = "/usr/local/share/tessdata")
    private val tessdataPath: String,

    @ConfigProperty(name = "ocr.language", defaultValue = "eng")
    private val language: String
) {

    /**
     * Extract text from image bytes using Tesseract OCR.
     *
     * @param imageBytes the raw image file bytes (PNG, JPG, WEBP)
     * @return the extracted text
     * @throws OcrException if OCR fails
     */
    fun extractText(imageBytes: ByteArray): String {
        val image: BufferedImage = try {
            ImageIO.read(ByteArrayInputStream(imageBytes))
                ?: throw OcrException("Could not read image — unsupported format or corrupt file")
        } catch (e: OcrException) {
            throw e
        } catch (e: Exception) {
            throw OcrException("Failed to read image: ${e.message}", e)
        }

        return try {
            val tesseract = Tesseract()
            tesseract.setDatapath(tessdataPath)
            tesseract.setLanguage(language)
            tesseract.doOCR(image)
        } catch (e: Exception) {
            throw OcrException("Tesseract OCR failed: ${e.message}", e)
        }
    }
}

/**
 * Exception thrown when OCR processing fails.
 */
class OcrException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
