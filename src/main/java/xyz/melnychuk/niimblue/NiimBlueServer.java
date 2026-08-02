package xyz.melnychuk.niimblue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimprint.AppException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NiimBlueServer {

    private static final Duration START_TIMEOUT = Duration.ofSeconds(15);

    private final Process process;

    @Getter
    private final String url;

    private NiimBlueServer(Process process, String url) {
        this.process = process;
        this.url = url;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public static NiimBlueServer start() {
        Path runtime = runtimeDir();
        if (runtime == null) {
            throw new AppException("Node runtime not found. Run `mvn generate-resources` to build it.");
        }

        int port = freePort();
        ProcessBuilder pb = new ProcessBuilder(
                runtime.resolve("node/bin/node").toString(),
                cliPath(runtime).toString(),
                "server", "-h", "127.0.0.1", "-p", String.valueOf(port));
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            forwardOutput(process);
            String baseUrl = "http://127.0.0.1:" + port;
            waitUntilReady(process, baseUrl);
            log.info("start(). NiimBlue server started on {}", baseUrl);
            return new NiimBlueServer(process, baseUrl);
        } catch (IOException e) {
            throw new AppException("Failed to start node server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Interrupted while starting node server", e);
        }
    }

    public void stop() {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private static void forwardOutput(Process process) {
        Thread thread = new Thread(() -> {
            try (InputStream in = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("forwardOutput(). {}", line);
                }
            } catch (IOException e) {
                log.error("Exception in forwardOutput().", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static void waitUntilReady(Process process, String baseUrl) throws InterruptedException {
        //TODO: вынести в NimBlueApi
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        long deadline = System.currentTimeMillis() + START_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw new AppException("Node server exited with code " + process.exitValue());
            }
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return;
                }
            } catch (Exception e) {
                log.warn("Exception in waitUntilReady().", e);
            }
            Thread.sleep(500);
        }
        throw new AppException("Niimblue server did not start within " + START_TIMEOUT.getSeconds() + "s");
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new AppException("Unable to find a free port", e);
        }
    }

    private static Path runtimeDir() {
        String property = System.getProperty("niimblue.runtime");
        if (property != null && !property.isBlank() && isRuntime(Path.of(property))) {
            return Path.of(property);
        }
        for (Path candidate : candidateRuntimeDirs()) {
            if (isRuntime(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static List<Path> candidateRuntimeDirs() {
        List<Path> dirs = new ArrayList<>();
        dirs.add(Path.of("").toAbsolutePath().resolve("runtime"));
        dirs.add(Path.of("").toAbsolutePath().resolve("target/runtime"));
        try {
            URI location = NiimBlueServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Path.of(location);
            if (Files.isDirectory(path)) {
                Path basedir = path.getParent().getParent();
                dirs.add(basedir.resolve("runtime"));
                dirs.add(basedir.resolve("target/runtime"));
            } else {
                Path image = path.getParent().getParent();
                dirs.add(image.resolve("runtime"));
            }
        } catch (Exception e) {
            log.error("Exception in candidateRuntimeDirs().", e);
        }
        return dirs;
    }

    private static boolean isRuntime(Path dir) {
        return Files.isExecutable(dir.resolve("node/bin/node"))
                && Files.isRegularFile(cliPath(dir));
    }

    private static Path cliPath(Path runtime) {
        return runtime.resolve("server/node_modules/@mmote/niimblue-node/cli.mjs");
    }
}
