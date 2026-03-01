package io.github.mdasifmustafa.sbx.config.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public final class DatabaseConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigLoader.class);

    @SuppressWarnings("unchecked")
    public DatabaseConfig load(Path baseFile, Path profileFile) {

        Map<String, Object> base =
            loadYaml(baseFile);

        Map<String, Object> profile =
            profileFile != null && Files.exists(profileFile)
                ? loadYaml(profileFile)
                : Map.of();

        // Overlay: profile overrides base
        Map<String, Object> merged = new HashMap<>(base);
        merged.putAll(profile);

        String defaultConnection =
            (String) merged.get("default");

        Map<String, DatabaseConnection> connections = new HashMap<>();

        Object rawConnections = merged.get("connections");
        if (rawConnections instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                if (!(entry.getValue() instanceof Map<?, ?> values)) {
                    throw new IllegalStateException(
                        "Invalid connection definition for: " + name
                    );
                }

                DatabaseConnection conn = new DatabaseConnection();
                populate(conn, (Map<String, Object>) values);
                connections.put(name, conn);
            }
        }

        return new DatabaseConfig(defaultConnection, connections);
    }

    // Convenience overload
    public DatabaseConfig load(Path file) {
        return load(file, null);
    }

    private Map<String, Object> loadYaml(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
        	LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(Map.class, options);
            Yaml yaml = new Yaml(constructor);
            Map<String, Object> data = yaml.load(in);
            return data == null ? Map.of() : data;
        } catch (IOException e) {
            logger.error("Failed to read YAML file: {}", file, e);
            throw new IllegalStateException(
                "Failed to read YAML file: " + file, e
            );
        }
    }

    private void populate(
        DatabaseConnection conn,
        Map<String, Object> values
    ) {
        String engine = (String) values.get("engine");
        if (engine == null) {
            throw new IllegalStateException(
                "Missing 'engine' field in connection"
            );
        }

        if (DatabaseEngineRegistry.get(engine) == null) {
            throw new IllegalStateException(
                "Unsupported database engine: " + engine
            );
        }

        conn.setEngine(engine);
        conn.setHost((String) values.get("host"));
        conn.setDatabase((String) values.get("database"));
        conn.setUsername((String) values.get("username"));
        conn.setPassword((String) values.get("password"));

        Object port = values.get("port");
        if (port != null) {
            if (port instanceof Number n) {
                conn.setPort(n.intValue());
            } else if (port instanceof String s) {
                try {
                    conn.setPort(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    throw new IllegalStateException(
                        "Invalid port value: " + s
                    );
                }
            } else {
                throw new IllegalStateException(
                    "Invalid port type: " + port
                );
            }
        }
    }
}