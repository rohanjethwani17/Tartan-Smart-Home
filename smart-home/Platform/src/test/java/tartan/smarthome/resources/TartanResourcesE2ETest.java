package tartan.smarthome.endToEnd;

import org.junit.jupiter.api.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class TartanResourcesE2ETest
{
    private static final String BASE_URL = "http://localhost:8080";
    private static final String HOUSE = "mse";
    private static final String USER = "admin";
    private static final String PASS = "1234";
    private static final String STATE_PATH  = "/smarthome/state/"  + HOUSE;
    private static final String UPDATE_PATH = "/smarthome/update/" + HOUSE;
    static HttpClient HTTP;

    private String basicAuthHeader(String user, String pass) {
        String token = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private static boolean isServiceReachable() {
        try (var socket = new java.net.Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 8080), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeAll
    static void setUpAll() {
        HTTP = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        Assumptions.assumeTrue(isServiceReachable(),
                "Smart Home service is not reachable on localhost:8080");
    }

    /**
     * Tests the user to be able to login in with the correct credentials
     * GET /state without auth -> 401
     * @throws Exception
     */
    @Test
    public void testAuth401() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + STATE_PATH))
                .header("Accept", "text/html")
                .GET().build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, res.statusCode());
    }

    /**
     * Tests that the user is logged in
     * GET /state with auth -> 200
     * @throws Exception
     */
    @Test
    public void testAuth200() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + STATE_PATH))
                .header("Accept", "text/html")
                .header("Authorization", basicAuthHeader(USER, PASS))
                .GET().build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertNotNull(res.body());
        assertFalse(res.body().isBlank());
    }

    /**
     * Tests that the user is logged in, in order to make any POST requests
     * POST /update without authentication -> 401, set temperature to 60
     * @throws Exception
     */
    @Test
    public void testUpdateTartanStatusAuth401() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + UPDATE_PATH))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"targetTemp\":\"60\"}", StandardCharsets.UTF_8))
                .build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, res.statusCode());
    }

    /**
     * Tests the user is logged in and able to update the status of the home
     * POST /update with authentication -> 200, set temperature to 60
     * @throws Exception
     */
    @Test
    public void testUpdateTartanStatusAuth200() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + UPDATE_PATH))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuthHeader(USER, PASS))
                .POST(HttpRequest.BodyPublishers.ofString("{\"targetTemp\":\"60\"}", StandardCharsets.UTF_8))
                .build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        assertEquals(200, res.statusCode());

        // check the html
        var get = HttpRequest.newBuilder(URI.create(BASE_URL + STATE_PATH))
                .header("Accept", "text/html")
                .header("Authorization", basicAuthHeader(USER, PASS))
                .GET().build();
        var getRes = HTTP.send(get, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getRes.statusCode());
        // Loosely check that the page mentions 60
        assertTrue(getRes.body().contains("60"),
                "State page should reflect updated target temperature");
    }


}
