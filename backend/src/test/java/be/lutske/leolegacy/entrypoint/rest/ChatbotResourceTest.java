package be.lutske.leolegacy.entrypoint.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class ChatbotResourceTest {

    @Test
    void postChatbotMessageReturnsLeonardoReply() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "message": "I would like a tomato based disch"
                        }
                        """)
                .when()
                .post("/api/chatbot/messages")
                .then()
                .statusCode(200)
                .body("author", is("Leonardo"))
                .body("message", notNullValue())
                .body("recommendations.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void postAliasBasedChatbotMessageReturnsRecommendations() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "message": "something with chicken"
                        }
                        """)
                .when()
                .post("/api/chatbot/messages")
                .then()
                .statusCode(200)
                .body("author", is("Leonardo"))
                .body("message", notNullValue())
                .body("recommendations.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void postOvenDishChatbotMessageReturnsRecommendations() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "message": "Recommend an oven dish"
                        }
                        """)
                .when()
                .post("/api/chatbot/messages")
                .then()
                .statusCode(200)
                .body("author", is("Leonardo"))
                .body("recommendations.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void postContextAwareChatbotMessageUsesConversationHistory() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "message": "make it vegetarian",
                          "context": [
                            { "role": "user", "message": "I would like a tomato based dish" }
                          ]
                        }
                        """)
                .when()
                .post("/api/chatbot/messages")
                .then()
                .statusCode(200)
                .body("author", is("Leonardo"))
                .body("message", notNullValue())
                .body("recommendations.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void postEmptyChatbotMessageReturnsIntroduction() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "message": ""
                        }
                        """)
                .when()
                .post("/api/chatbot/messages")
                .then()
                .statusCode(200)
                .body("author", is("Leonardo"))
                .body("recommendations.size()", is(0));
    }
}
