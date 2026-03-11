package be.lutske.leolegacy.entrypoint.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class VersionResourceTest {

    @Test
    void getApiVersionReturns200WithJsonVersion() {
        given()
                .when()
                .get("/api/version")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("version", is(notNullValue()))
                .body("version", is("1.2.3"));
    }

    @Test
    void getApiVersionResponseHasCorrectContentType() {
        given()
                .when()
                .get("/api/version")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }
}
