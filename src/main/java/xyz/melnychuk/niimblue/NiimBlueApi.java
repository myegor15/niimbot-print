package xyz.melnychuk.niimblue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.melnychuk.niimblue.request.ConnectRequest;
import xyz.melnychuk.niimblue.request.PrintRequest;
import xyz.melnychuk.niimblue.request.ScanRequest;
import xyz.melnychuk.niimblue.response.ConnectedResponse;
import xyz.melnychuk.niimblue.response.DevicesResponse;
import xyz.melnychuk.niimblue.response.InfoResponse;
import xyz.melnychuk.niimblue.response.RfidResponse;
import xyz.melnychuk.niimprint.exception.AppException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NiimBlueApi {

    private final String url;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public NiimBlueApi(String url) {
        this.url = url.replaceAll("/+$", "");
        this.client = getClient();
        this.mapper = getMapper();
    }

    public void connect(String transport, String address) {
        postJson("/connect", new ConnectRequest(transport, address), JsonNode.class);
    }

    public void disconnect() {
        postJson("/disconnect", mapper.createObjectNode(), JsonNode.class);
    }

    public boolean isConnected() {
        return get("/connected", ConnectedResponse.class).connected();
    }

    public InfoResponse info() {
        return get("/info", InfoResponse.class);
    }

    public RfidResponse rfid() {
        return get("/rfid", RfidResponse.class);
    }

    public DevicesResponse scan() {
        return postJson("/scan", new ScanRequest("ble"), DevicesResponse.class);
    }

    public void print(PrintRequest request) {
        postJson("/print", request, JsonNode.class);
    }

    private HttpClient getClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private <T> T get(String path, Class<T> type) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url + path))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            return mapper.readValue(check(client.send(req, HttpResponse.BodyHandlers.ofString())), type);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    private <T> T postJson(String path, Object body, Class<T> type) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url + path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return mapper.readValue(check(client.send(req, HttpResponse.BodyHandlers.ofString())), type);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    private String check(HttpResponse<String> resp) throws NiimBlueApiException {
        if (resp.statusCode() >= 400) {
            throw new NiimBlueApiException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

}
