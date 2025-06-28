package org.bugtracker;

import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;


@Disabled("Start the application and then enable this test")
class BugTrackerApplicationTests {

    @Test
    void contextLoads() {
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = 8081;
        RestAssured.baseURI = "http://localhost:8081";
    }

    @Test
    void johnShouldNotSeeTheDeleteButton(){
        var config = new FormAuthConfig("/auth", "user", "pass")
                .withLoggingEnabled();

        String responseString = given()
                .contentType(ContentType.URLENC)
                .auth().form("john", "doe", config)
                .when().get("/singer/1")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        Assertions.assertAll(
                () -> Assertions.assertTrue(responseString.contains("<div class=\"card-header\">User Details</div>")),
                () -> Assertions.assertTrue(responseString.contains("<td>Mayer</td>")),
                () -> Assertions.assertFalse(responseString.contains("Delete"))
        );
    }

    @Test
    void johnShouldNotBeAllowedToDeleteSinger(){
        var config = new FormAuthConfig("/auth", "user", "pass");

        String responseString = given()
                .contentType(ContentType.URLENC)
                .auth().form("john", "doe", config)
                .when().get("/singer/1")
                .then()
                .assertThat().statusCode(HttpStatus.FORBIDDEN.value())
                .extract().body().asString();
    }

    @Test
    void adminShouldSeeTheDeleteButton(){
        var config = new FormAuthConfig("/auth", "user", "pass");

        String responseString = given()
                .contentType(ContentType.URLENC)
                .auth().form("admin", "admin", config)
                .when().get("/singer/1")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        Assertions.assertAll(
                () -> Assertions.assertTrue(responseString.contains("<div class=\"card-header\">User Details</div>")),
                () -> Assertions.assertTrue(responseString.contains("<td>Mayer</td>")),
                () -> Assertions.assertTrue(responseString.contains("Delete"))
        );
    }
}