package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class HomePageAvailabilityTest {

    @Test
    void rootShouldServeHomePage() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(containsString("Leo Legacy Recepten"));
    }
}
