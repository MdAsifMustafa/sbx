package io.github.mdasifmustafa.sbx.config.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabasePropertiesReader {

    private static final Pattern DEFAULT_KEY =
        Pattern.compile("^spring\\.datasource\\.(url|username|password)$");

    private static final Pattern NAMED_KEY =
        Pattern.compile("^spring\\.datasource\\.([^.]+)\\.(url|username|password)$");

    public DatabaseConfig read(Path propertiesFile) {
        Properties props = new Properties();

        try (InputStream in = Files.newInputStream(propertiesFile)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read properties file: " + propertiesFile, e
            );
        }

        Map<String, DatabaseConnection> connections = new HashMap<>();

        // ---- Default (primary) ----
        DatabaseConnection primary = new DatabaseConnection();
        boolean hasPrimary = false;

        for (String key : props.stringPropertyNames()) {
            Matcher m = DEFAULT_KEY.matcher(key);
            if (!m.matches()) {
                continue;
            }
            populate(primary, m.group(1), props.getProperty(key));
            hasPrimary = true;
        }

        if (hasPrimary) {
            connections.put("primary", primary);
        }

        // ---- Named datasources ----
        for (String key : props.stringPropertyNames()) {
            Matcher m = NAMED_KEY.matcher(key);
            if (!m.matches()) {
                continue;
            }

            String name = m.group(1);
            String field = m.group(2);

            DatabaseConnection conn =
                connections.computeIfAbsent(name, n -> new DatabaseConnection());

            populate(conn, field, props.getProperty(key));
        }

        return new DatabaseConfig("primary", connections);
    }

    private void populate(
        DatabaseConnection conn,
        String field,
        String value
    ) {
        switch (field) {
            case "url" -> conn.setUrl(value);
            case "username" -> conn.setUsername(value);
            case "password" -> conn.setPassword(value);
        }
    }
}