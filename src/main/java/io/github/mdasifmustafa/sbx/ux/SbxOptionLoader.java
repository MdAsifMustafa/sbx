package io.github.mdasifmustafa.sbx.ux;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SbxOptionLoader {

    private static final Logger logger = LoggerFactory.getLogger(SbxOptionLoader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static JsonNode ROOT;

    static {
        try (InputStream in =
                     SbxOptionLoader.class
                             .getClassLoader()
                             .getResourceAsStream("options.json")) {

            if (in == null) {
                throw new IllegalStateException("options.json not found in classpath");
            }

            ROOT = MAPPER.readTree(in);
        } catch (IOException e) {
            logger.error("Failed to load options.json", e);
            throw new IllegalStateException("Failed to load options.json", e);
        }
    }

    private SbxOptionLoader() {}

    // --------------------------------------------------
    // Existing API (unchanged)
    // --------------------------------------------------

    public static SbxOptionSection getSection(String name) {
        JsonNode section = ROOT.get(name);
        if (section == null || !section.isArray()) {
            throw new IllegalArgumentException("Invalid option section: " + name);
        }

        List<String> options = new ArrayList<>();
        Iterator<JsonNode> it = section.elements();
        while (it.hasNext()) {
            options.add(it.next().asText());
        }

        return new SbxOptionSection(name, options);
    }

    // --------------------------------------------------
    // NEW API (used by AppNewCommand)
    // --------------------------------------------------

    public static List<String> legacyBootVersions() {
        return readStringArray("bootVersion.legacy");
    }

    public static List<Integer> javaCompatibility(String bootVersion) {
        JsonNode jc = ROOT.get("javaCompatibility");
        if (jc == null) return List.of();

        JsonNode arr = jc.get(bootVersion);
        if (arr == null || !arr.isArray()) return List.of();

        List<Integer> values = new ArrayList<>();
        for (JsonNode n : arr) {
            values.add(n.asInt());
        }
        return values;
    }

    public static List<String> configFormats() {
        return readStringArray("configFormat");
    }

    // --------------------------------------------------
    // Internal helpers
    // --------------------------------------------------

    private static List<String> readStringArray(String path) {
        JsonNode node = resolve(path);

        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode n : node) {
                values.add(n.asText());
            }
        }
        return values;
    }

    /**
     * Resolves dot-notated paths like:
     *   bootVersion.legacy
     *   javaCompatibility.2.7.18
     */
    private static JsonNode resolve(String path) {
        String[] parts = path.split("\\.");
        JsonNode current = ROOT;

        for (String p : parts) {
            if (current == null) {
                return null;
            }
            current = current.get(p);
        }
        return current;
    }
}