package io.github.mdasifmustafa.sbx.config.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class DatabaseConfigWriter {

    private final Yaml yaml;

    public DatabaseConfigWriter() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setWidth(120);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        this.yaml = new Yaml(options);
    }

    /* =========================
       Public API
       ========================= */

    public void set(
        Path file,
        String name,
        DatabaseConnection conn,
        boolean makeDefault
    ) {
        Map<String, Object> root = load(file);

        Map<String, Object> connections =
            getOrCreateConnections(root);

        connections.put(name, serialize(conn));

        if (makeDefault) {
            root.put("default", name);
        }

        store(file, root);
    }

    public void use(Path file, String name) {
        Map<String, Object> root = load(file);

        Map<String, Object> connections =
            getOrCreateConnections(root);

        if (!connections.containsKey(name)) {
            throw new IllegalStateException(
                "Connection '" + name + "' does not exist"
            );
        }

        root.put("default", name);
        store(file, root);
    }

    public void remove(Path file, String name) {
        Map<String, Object> root = load(file);

        Map<String, Object> connections =
            getOrCreateConnections(root);

        if (!connections.containsKey(name)) {
            return;
        }

        connections.remove(name);

        Object currentDefault = root.get("default");
        if (name.equals(currentDefault)) {
            root.remove("default");
        }

        store(file, root);
    }

    /* =========================
       Internal helpers
       ========================= */

    @SuppressWarnings("unchecked")
    private Map<String, Object> load(Path file) {
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }

        try (InputStream in = Files.newInputStream(file)) {
            Object data = yaml.load(in);
            if (data == null) {
                return new LinkedHashMap<>();
            }
            return (Map<String, Object>) data;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to read YAML file: " + file, e
            );
        }
    }

    private void store(Path file, Map<String, Object> root) {
        try (OutputStream out = Files.newOutputStream(file);
             OutputStreamWriter writer =
                 new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            yaml.dump(root, writer);

        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to write YAML file: " + file, e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateConnections(
        Map<String, Object> root
    ) {
        Object raw = root.get("connections");
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        Map<String, Object> connections = new LinkedHashMap<>();
        root.put("connections", connections);
        return connections;
    }

    private Map<String, Object> serialize(DatabaseConnection conn) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (conn.getEngine() != null) {
            map.put("engine", conn.getEngine());
        }
        if (conn.getHost() != null) {
            map.put("host", conn.getHost());
        }
        if (conn.getPort() != null) {
            map.put("port", conn.getPort());
        }
        if (conn.getDatabase() != null) {
            map.put("database", conn.getDatabase());
        }
        if (conn.getUsername() != null) {
            map.put("username", conn.getUsername());
        }
        if (conn.getPassword() != null) {
            map.put("password", conn.getPassword());
        }

        return map;
    }
}