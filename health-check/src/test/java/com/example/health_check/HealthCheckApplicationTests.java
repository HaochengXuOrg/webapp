package com.example.health_check;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class HealthCheckApplicationTests {

	private static final String BASE_URL = "http://localhost:8080";

	@BeforeAll
	static void setup() {
		RestAssured.baseURI = BASE_URL;
	}

	@Test
	@DisplayName("Test Health Check Success - 200 OK")
	void testHealthCheckSuccess() {
		given()
				.when()
				.get("/healthz")
				.then()
				.statusCode(200)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	// @Test
	// @DisplayName("Test Health Check Failure (503 Service Unavailable)")
	// void testHealthCheckFailure() {
	// 	//when(healthCheckService.recordHealthCheck()).thenReturn(False);

	// 	given()
	// 			.when()
	// 			.get("/healthz")
	// 			.then()
	// 			.statusCode(503)
	// 			.header("Cache-Control", containsString("no-cache"))
	// 			.header("Pragma", "no-cache")
	// 			.header("X-Content-Type-Options", "nosniff")
	// 			.body(isEmptyOrNullString());
	// }

	@Test
	@DisplayName("Test Bad Request for Query Parameter (400)")
	void testBadRequestForQueryParam() {
		given()
				// Add some query param
				.queryParam("foo", "bar")
				.when()
				.get("/healthz")
				.then()
				.statusCode(400)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	@Test
	@DisplayName("Test Bad Request for Payload (400)")
	void testBadRequestForPayload() {
		given()
				.body("{\"key\": \"value\"}")
				.when()
				.get("/healthz")
				.then()
				.statusCode(400)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	@Test
	@DisplayName("Test Method Not Allowed (POST -> 405)")
	void testMethodNotAllowed_Post() {
		given()
				.when()
				.post("/healthz")
				.then()
				.statusCode(405)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	@Test
	@DisplayName("Test Method Not Allowed (PUT -> 405)")
	void testMethodNotAllowed_Put() {
		given()
				.when()
				.put("/healthz")
				.then()
				.statusCode(405)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	@Test
	@DisplayName("Test Method Not Allowed (DELETE -> 405)")
	void testMethodNotAllowed_Delete() {
		given()
				.when()
				.delete("/healthz")
				.then()
				.statusCode(405)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}

	@Test
	@DisplayName("Test Method Not Allowed (PATCH -> 405)")
	void testMethodNotAllowed_Patch() {
		given()
				.when()
				.patch("/healthz")
				.then()
				.statusCode(405)
				.header("Cache-Control", containsString("no-cache"))
				.header("Pragma", "no-cache")
				.header("X-Content-Type-Options", "nosniff")
				.body(isEmptyOrNullString());
	}
}
