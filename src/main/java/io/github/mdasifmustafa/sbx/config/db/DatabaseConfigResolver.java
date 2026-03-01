package io.github.mdasifmustafa.sbx.config.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public final class DatabaseConfigResolver {

    public enum Format {
        PROPERTIES,
        YAML
    }

    public static final class Resolution {
        private final Format format;
        private final Path file;

        public Resolution(Format format, Path file) {
            this.format = format;
            this.file = file;
        }

        public Format getFormat() {
            return format;
        }

        public Path getFile() {
            return file;
        }
    }

    private static final Path RESOURCES =
        Path.of("src/main/resources");

    public Resolution resolve(String profile) {

        try {
            Files.createDirectories(RESOURCES);

            // ---------- Detect project type ----------
            boolean propertiesProject = isPropertiesProject(profile);

            String suffix = profile != null && !profile.isBlank()
                ? "-" + profile
                : "";

            if (propertiesProject) {

                Path dbFile =
                    RESOURCES.resolve("database" + suffix + ".properties");

                createIfMissing(dbFile);

                linkProperties(profile, dbFile);

                return new Resolution(Format.PROPERTIES, dbFile);

            } else {

                Path dbFile =
                    RESOURCES.resolve("database" + suffix + ".yml");

                createIfMissing(dbFile);

                linkYaml(profile, dbFile);

                return new Resolution(Format.YAML, dbFile);
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to resolve database config", e
            );
        }
    }

    // ---------------------------------------------------

    private boolean isPropertiesProject(String profile) {

        if (profile != null && !profile.isBlank()) {
            if (Files.exists(
                RESOURCES.resolve("application-" + profile + ".properties")
            )) {
                return true;
            }
        }

        if (Files.exists(RESOURCES.resolve("application.properties"))) {
            return true;
        }

        if (profile != null && !profile.isBlank()) {
            if (Files.exists(
                RESOURCES.resolve("application-" + profile + ".yml")
            )) {
                return false;
            }
        }

        if (Files.exists(RESOURCES.resolve("application.yml"))) {
            return false;
        }

        // default → YAML
        return false;
    }

    private void createIfMissing(Path file) throws Exception {
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    private void linkProperties(String profile, Path dbFile)
        throws Exception {

        Path appFile = profile != null && !profile.isBlank()
            ? RESOURCES.resolve("application-" + profile + ".properties")
            : RESOURCES.resolve("application.properties");

        createIfMissing(appFile);

        String importLine =
            "spring.config.import=optional:classpath:" +
            dbFile.getFileName();

        String content = Files.readString(appFile);

        if (!content.contains("spring.config.import")) {
            if (!content.isBlank()) {
                content += System.lineSeparator();
            }
            content += importLine;
            Files.writeString(
                appFile,
                content,
                StandardCharsets.UTF_8
            );
        }
    }

    private void linkYaml(String profile, Path dbFile)
        throws Exception {

        Path appFile = profile != null && !profile.isBlank()
            ? RESOURCES.resolve("application-" + profile + ".yml")
            : RESOURCES.resolve("application.yml");

        createIfMissing(appFile);

        String content = Files.readString(appFile);

        String importBlock =
            "spring:\n" +
            "  config:\n" +
            "    import: optional:classpath:" +
            dbFile.getFileName();

        if (!content.contains("spring.config.import")) {
            if (!content.isBlank()) {
                content += "\n\n";
            }
            content += importBlock;
            Files.writeString(appFile, content);
        }
    }
}