package io.github.mdasifmustafa.sbx.config.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DatabaseEngineRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEngineRegistry.class);

    private static final String RESOURCE =
        "/database_engines.json";

    private static final Registry DATA;

    static {
        try (InputStream in =
                 DatabaseEngineRegistry.class
                     .getResourceAsStream(RESOURCE)) {

            if (in == null) {
                throw new IllegalStateException(
                    "Missing resource: " + RESOURCE
                );
            }

            ObjectMapper mapper = new ObjectMapper();
            DATA = mapper.readValue(in, Registry.class);

        } catch (IOException e) {
            logger.error("Failed to load database engine registry", e);
            throw new IllegalStateException(
                "Failed to load database engine registry", e
            );
        }
    }

    /* =========================
       Public API
       ========================= */

    public static Map<String, DatabaseEngine> allEngines() {
        return Collections.unmodifiableMap(DATA.engines);
    }

    public static Set<String> supportedEngines() {
        return DATA.engines.entrySet().stream()
            .filter(e -> e.getValue().enabledByDefault)
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(
                java.util.LinkedHashSet::new
            ));
    }

    public static DatabaseEngine get(String key) {
        return DATA.engines.get(key);
    }

    public static String defaultDatabase() {
        return DATA.defaults.database;
    }

    public static String defaultUsername() {
        return DATA.defaults.username;
    }

    public static String defaultHost() {
        return DATA.defaults.host;
    }

    /* =========================
       Internal JSON mapping
       ========================= */

    private static final class Registry {
        public Defaults defaults;
        public Map<String, DatabaseEngine> engines =
            new LinkedHashMap<>();
    }

    private static final class Defaults {
        public String database;
        public String username;
        public String host;
    }

    private DatabaseEngineRegistry() {}
}