package io.sudhanshugupta.moneytransfer;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class accountResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().header(new Header("accountId", "1")).contentType("application/json").get("/account/balance")
          .then()
             .statusCode(200);
    }

}