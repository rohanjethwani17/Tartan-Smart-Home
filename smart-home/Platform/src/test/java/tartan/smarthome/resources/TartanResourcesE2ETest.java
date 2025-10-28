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

    @Test
    public void testAuth401() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + STATE_PATH))
                .header("Accept", "text/html")
                .GET().build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, res.statusCode());
    }

    @Test
    void testAuth200() throws Exception{
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + STATE_PATH))
                .header("Accept", "text/html")
                .header("Authorization", basicAuthHeader(USER, PASS))
                .GET().build();

        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertNotNull(res.body());
        assertFalse(res.body().isBlank());
    }


}
