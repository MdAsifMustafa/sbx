package io.github.mdasifmustafa.sbx.initializr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SpringInitializrDependencyMetadataClient {

    private static final Logger logger = LoggerFactory.getLogger(SpringInitializrDependencyMetadataClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path CACHE =
            Path.of(System.getProperty("user.home"),
                    ".sbx", "cache", "initializr-metadata.json");

    private SpringInitializrDependencyMetadataClient() {}

    public static List<InitializrDependencyGroup> fetch() {
        try {
            JsonNode root = MAPPER.readTree(Files.readString(CACHE));
            JsonNode deps = root.get("dependencies").get("values");

            List<InitializrDependencyGroup> groups = new ArrayList<>();

            for (JsonNode g : deps) {
                String name = g.get("name").asText();
                List<InitializrDependency> list = new ArrayList<>();

                for (JsonNode d : g.get("values")) {
                    list.add(new InitializrDependency(
                            d.get("id").asText(),
                            d.get("name").asText(),
                            d.get("description").asText()
                    ));
                }
                groups.add(new InitializrDependencyGroup(name, list));
            }
            return groups;
        } catch (IOException e) {
            logger.error("Failed to load Spring Initializr dependency metadata", e);
            throw new IllegalStateException(
                    "Failed to load Spring Initializr dependency metadata", e);
        }
    }
}