package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class RecipeImportResourceTest {

    @Test
    void shouldReturnNeedsMoreInfoForAmbiguousImport() {
        byte[] pngBytes = createPngBytes();

        given()
                .multiPart("file", "recipe.png", pngBytes, "image/png")
                .when().post("/api/recipes/import")
                .then()
                .statusCode(anyOf(equalTo(422), equalTo(503)));
    }

    @Test
    void shouldCreateRecipeWhenConfirmPayloadIsComplete() {
        Map<String, Object> payload = Map.of(
                "rawText", "Scanned recipe",
                "proposedRecipe", Map.of(
                        "title", "OCR Tomato Soup",
                        "description", "Simple soup",
                        "servings", 2,
                        "ingredients", "2 tomatoes\n1 onion",
                        "instructions", "Cook everything for 20 minutes",
                        "tags", "soup,quick",
                        "source", "Imported from image"
                ),
                "userOverrides", Map.of(
                        "title", "OCR Tomato Soup",
                        "description", "Simple soup",
                        "servings", 2,
                        "ingredients", "2 tomatoes\n1 onion",
                        "instructions", "Cook everything for 20 minutes",
                        "tags", "soup,quick",
                        "source", "Imported from image",
                        "notes", "Adjusted typo"
                )
        );

        given()
                .contentType("application/json")
                .body(payload)
                .when().post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("OCR Tomato Soup"))
                .body("source", equalTo("Imported from image"));
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
}
