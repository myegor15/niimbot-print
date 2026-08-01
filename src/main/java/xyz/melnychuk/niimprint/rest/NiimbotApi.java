package xyz.melnychuk.niimprint.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NiimbotApi {
    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public NiimbotApi(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public void connect(String transport, String address) throws Exception {
        postJson("/connect", new ConnectRequest(transport, address));
    }

    public void disconnect() throws Exception {
        postJson("/disconnect", mapper.createObjectNode());
    }

    public boolean isConnected() throws Exception {
        return get("/connected").get("connected").asBoolean();
    }

    public JsonNode info() throws Exception {
        return get("/info");
    }

    public JsonNode rfid() throws Exception {
        return get("/rfid");
    }

    public DevicesResponse scan() throws Exception {
        JsonNode body = postJson("/scan", new ScanRequest("ble"));
        return mapper.convertValue(body, DevicesResponse.class);
    }

    public void print(PrintRequest request) throws Exception {
        postJson("/print", request);
    }

    private JsonNode get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .GET().build();
        return mapper.readTree(check(http.send(req, HttpResponse.BodyHandlers.ofString())));
    }

    private JsonNode postJson(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return mapper.readTree(check(http.send(req, HttpResponse.BodyHandlers.ofString())));
    }

    private String check(HttpResponse<String> resp) throws ApiException {
        if (resp.statusCode() >= 400) {
            throw new ApiException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}
