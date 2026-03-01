package io.github.mdasifmustafa.sbx.initializr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SpringInitializrMetadataClient {

    private static final Logger logger = LoggerFactory.getLogger(SpringInitializrMetadataClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path CACHE =
            Path.of(System.getProperty("user.home"),
                    ".sbx", "cache", "initializr-metadata.json");

    private SpringInitializrMetadataClient() {}

    public static SpringInitializrMetadata fetch() {
        try {
            JsonNode root = loadMetadataJson();
            return new SpringInitializrMetadata(
                    parseSingle(root, "type"),        // 👈 ADD THIS
                    parseSingle(root, "packaging"),
                    parseSingle(root, "javaVersion"),
                    parseSingle(root, "language"),
                    parseSingle(root, "bootVersion")
            );
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to load Spring Initializr metadata", e);
            throw new IllegalStateException("Failed to load Spring Initializr metadata", e);
        }
    }

    // --------------------------------------------------

    private static JsonNode loadMetadataJson() throws IOException, InterruptedException {
        if (Files.exists(CACHE) &&
            Files.getLastModifiedTime(CACHE).toInstant()
                    .isAfter(Instant.now().minusSeconds(24 * 3600))) {

            try (InputStream in = Files.newInputStream(CACHE)) {
                return MAPPER.readTree(in);
            }
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(
                URI.create("https://start.spring.io/metadata/client"))
                .GET()
                .build();

        HttpResponse<InputStream> res =
                client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        Files.createDirectories(CACHE.getParent());
        Files.copy(res.body(), CACHE, StandardCopyOption.REPLACE_EXISTING);

        try (InputStream in = Files.newInputStream(CACHE)) {
            return MAPPER.readTree(in);
        }
    }

    private static InitializrSingleSelect parseSingle(JsonNode root, String key) {
        JsonNode node = root.get(key);

        String defaultId = node.get("default").asText();
        List<InitializrOption> values = new ArrayList<>();

        for (JsonNode v : node.get("values")) {
            values.add(new InitializrOption(
                    v.get("id").asText(),
                    v.get("name").asText(),
                    v.get("action")  != null ? v.get("action").asText() : null,
                    v.get("id").asText().equals(defaultId)
            ));
        }

        return new InitializrSingleSelect(key, defaultId, values);
    }
}