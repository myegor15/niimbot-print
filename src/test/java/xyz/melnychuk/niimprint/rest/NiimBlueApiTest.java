package xyz.melnychuk.niimprint.rest;

import org.junit.jupiter.api.Test;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimblue.NiimBlueApiException;
import xyz.melnychuk.niimblue.request.PrintRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NiimBlueApiTest {

    private static class MockServer implements AutoCloseable {
        private final ServerSocket server;
        private final List<String> requests = new CopyOnWriteArrayList<>();
        private volatile String responseBody = "{}";
        private volatile int status = 200;
        private Thread thread;

        MockServer() throws IOException {
            server = new ServerSocket(0);
            thread = new Thread(this::acceptLoop);
            thread.setDaemon(true);
            thread.start();
        }

        private void acceptLoop() {
            try {
                while (!server.isClosed()) {
                    try (Socket socket = server.accept()) {
                        handle(socket);
                    }
                }
            } catch (IOException ignored) {
                // server closed
            }
        }

        private void handle(Socket socket) throws IOException {
            InputStream in = socket.getInputStream();
            String head = readHeader(in);
            requests.add(head);

            int length = 0;
            for (String line : head.split("\r\n")) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    length = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            if (length > 0) {
                requests.add(new String(in.readNBytes(length), StandardCharsets.UTF_8));
            }

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            String response = "HTTP/1.1 " + status + " OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + bytes.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            OutputStream out = socket.getOutputStream();
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.write(bytes);
            out.flush();
        }

        private static String readHeader(InputStream in) throws IOException {
            StringBuilder sb = new StringBuilder();
            int b;
            while ((b = in.read()) != -1) {
                sb.append((char) b);
                int len = sb.length();
                if (len >= 4 && "\r\n\r\n".equals(sb.substring(len - 4))) {
                    return sb.toString();
                }
            }
            return sb.toString();
        }

        String baseUrl() {
            return "http://localhost:" + server.getLocalPort();
        }

        List<String> requests() {
            return new ArrayList<>(requests);
        }

        void respond(int status, String body) {
            this.status = status;
            this.responseBody = body;
        }

        @Override
        public void close() throws IOException {
            server.close();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }

    @Test
    void connectSendsExpectedRequest() throws Exception {
        try (MockServer mock = new MockServer()) {
            mock.respond(200, "{\"message\":\"Connected\"}");
            NiimBlueApi api = new NiimBlueApi(mock.baseUrl());

            api.connect("ble", "27:03:07:17:6e:82");

            List<String> req = mock.requests();
            assertTrue(req.get(0).startsWith("POST /connect HTTP/1.1"));
            assertTrue(req.get(1).contains("\"transport\":\"ble\""));
            assertTrue(req.get(1).contains("\"address\":\"27:03:07:17:6e:82\""));
        }
    }

    @Test
    void connectedParsesStatus() throws Exception {
        try (MockServer mock = new MockServer()) {
            mock.respond(200, "{\"connected\":true}");
            assertTrue(new NiimBlueApi(mock.baseUrl()).isConnected());
            mock.respond(200, "{\"connected\":false}");
            assertFalse(new NiimBlueApi(mock.baseUrl()).isConnected());
        }
    }

    @Test
    void printSendsImagePayload() throws Exception {
        try (MockServer mock = new MockServer()) {
            mock.respond(200, "{\"message\":\"Printed\"}");
            NiimBlueApi api = new NiimBlueApi(mock.baseUrl());

            api.print(PrintRequest.of("QUJD", 384, 240, 3, 1, "top"));

            List<String> req = mock.requests();
            assertTrue(req.get(0).startsWith("POST /print HTTP/1.1"));
            assertTrue(req.get(1).contains("\"imageBase64\":\"QUJD\""));
            assertTrue(req.get(1).contains("\"labelWidth\":384"));
            assertTrue(req.get(1).contains("\"printDirection\":\"top\""));
        }
    }

    @Test
    void serverErrorThrowsApiException() throws Exception {
        try (MockServer mock = new MockServer()) {
            mock.respond(500, "{\"message\":\"Not connected\"}");
            NiimBlueApi api = new NiimBlueApi(mock.baseUrl());

            assertThrows(NiimBlueApiException.class, () -> api.print(
                    PrintRequest.of("QUJD", 384, 240, 3, 1, "top")));
        }
    }
}
