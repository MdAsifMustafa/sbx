package io.github.mdasifmustafa.sbx.config.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class SpringApplicationWriter {

    private final Yaml yaml;

    public SpringApplicationWriter() {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        this.yaml = new Yaml(options);
    }

    /* ===================================================== */

    public void setJpaDialect(
            DatabaseConfigResolver.Resolution resolution,
            String dialect
    ) {

        Path appFile = resolveApplicationFile(resolution);

        if (resolution.getFormat()
                == DatabaseConfigResolver.Format.PROPERTIES) {

            writeProperties(appFile, dialect);

        } else {

            writeYaml(appFile, dialect);
        }
    }

    /* ===================================================== */

    private Path resolveApplicationFile(
            DatabaseConfigResolver.Resolution resolution
    ) {

        Path dbFile = resolution.getFile();

        String name = dbFile.getFileName().toString();

        String appName =
                name.replace("database", "application");

        return dbFile.getParent().resolve(appName);
    }

    /* ================= PROPERTIES ================= */

    private void writeProperties(Path file, String dialect) {

        try {

            Properties props = new Properties();

            if (Files.exists(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    props.load(in);
                }
            }

            props.setProperty(
                    "spring.jpa.database-platform",
                    dialect
            );

            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, null);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to write Spring properties", e);
        }
    }

    /* ================= YAML ================= */

    @SuppressWarnings("unchecked")
    private void writeYaml(Path file, String dialect) {

        Map<String, Object> root = new LinkedHashMap<>();

        try {

            if (Files.exists(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    Object data = yaml.load(in);
                    if (data instanceof Map<?, ?> map) {
                        root = (Map<String, Object>) map;
                    }
                }
            }

            Map<String, Object> spring =
                    (Map<String, Object>) root.computeIfAbsent(
                            "spring", k -> new LinkedHashMap<>());

            Map<String, Object> jpa =
                    (Map<String, Object>) spring.computeIfAbsent(
                            "jpa", k -> new LinkedHashMap<>());

            jpa.put("database-platform", dialect);

            try (OutputStream out = Files.newOutputStream(file);
                 OutputStreamWriter writer =
                         new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

                yaml.dump(root, writer);
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to write Spring YAML", e);
        }
    }
}