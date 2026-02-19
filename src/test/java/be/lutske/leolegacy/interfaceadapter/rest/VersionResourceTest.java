package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class VersionResourceTest {

    @Test
    void shouldReturnConfiguredVersion() {
        given()
                .when().get("/api/version")
                .then()
                .statusCode(200)
                .body("version", equalTo("1.2.3"));
    }
}
