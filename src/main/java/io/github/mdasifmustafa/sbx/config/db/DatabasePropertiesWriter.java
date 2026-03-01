package io.github.mdasifmustafa.sbx.config.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabasePropertiesWriter {

    private static final Pattern DEFAULT_KEY =
        Pattern.compile("^spring\\.datasource\\.(url|username|password)$");

    /* =========================
       Public API
       ========================= */

    public void set(Path file, String name, DatabaseConnection conn) {
        Properties props = load(file);
        writeNamed(props, name, conn);
        store(file, props);
    }

    public void promoteToDefault(
        Path file,
        String newName,
        DatabaseConnection newConn,
        String backupName
    ) {
        Properties props = load(file);

        if (backupName != null && !backupName.isBlank()) {
            backupDefault(props, backupName);
        }

        removeDefault(props);
        writeDefault(props, newConn);

        store(file, props);
    }

    public void use(Path file, String name) {
        Properties props = load(file);

        if (!hasNamed(props, name)) {
            throw new IllegalStateException(
                "Named datasource '" + name + "' does not exist"
            );
        }

        DatabaseConnection conn = extractNamed(props, name);

        removeDefault(props);
        writeDefault(props, conn);

        // intentionally destructive
        removeNamed(props, name);

        store(file, props);
    }
    
    public void useWithBackup(
    	    Path file,
    	    String name,
    	    String backupName
    	) {
    	    Properties props = load(file);

    	    // Validate existence using existing public behavior
    	    DatabaseConnection conn = extractNamed(props, name);
    	    if (conn.getUrl() == null) {
    	        throw new IllegalStateException(
    	            "Named datasource '" + name + "' does not exist"
    	        );
    	    }

    	    // Backup via existing PUBLIC API (set)
    	    if (backupName != null && !backupName.isBlank()) {
    	        DatabaseConnection currentDefault =
    	            extractDefault(props);

    	        if (currentDefault.getUrl() != null) {
    	            set(file, backupName, currentDefault);
    	            // reload after write
    	            props = load(file);
    	        }
    	    }

    	    // Switch default using existing logic
    	    removeDefault(props);
    	    writeDefault(props, conn);
    	    removeNamed(props, name);

    	    store(file, props);
    	}
    private DatabaseConnection extractDefault(Properties props) {
        DatabaseConnection conn = new DatabaseConnection();

        props.forEach((k, v) -> {
            Matcher m = DEFAULT_KEY.matcher(k.toString());
            if (m.matches()) {
                populate(conn, m.group(1), v.toString());
            }
        });

        return conn;
    }
    public void rename(Path file, String oldName, String newName) {
        if ("primary".equals(oldName)) {
            throw new IllegalStateException(
                "Cannot rename default datasource in properties format"
            );
        }

        Properties props = load(file);

        DatabaseConnection conn = extractNamed(props, oldName);
        if (conn.getUrl() == null) {
            throw new IllegalStateException(
                "Named datasource '" + oldName + "' does not exist"
            );
        }

        // Remove old keys
        removeNamed(props, oldName);

        // Write under new name
        writeNamed(props, newName, conn);

        store(file, props);
    }
    

    public void remove(Path file, String name) {
        if ("primary".equals(name)) {
            throw new IllegalStateException(
                "Cannot remove default datasource"
            );
        }

        Properties props = load(file);
        removeNamed(props, name);
        store(file, props);
    }

    /* =========================
       Internals
       ========================= */

    private Properties load(Path file) {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to read properties file: " + file, e
            );
        }
        return props;
    }

    private void store(Path file, Properties props) {
        try {
            List<String> lines = new ArrayList<>();

            for (String key : new TreeSet<>(props.stringPropertyNames())) {
                String value = props.getProperty(key);
                lines.add(key + "=" + value);
            }

            Files.write(
                file,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to write properties file: " + file, e
            );
        }
    }

    private void writeDefault(Properties props, DatabaseConnection conn) {
        write(props, "spring.datasource.", conn);
    }

    private void writeNamed(
        Properties props,
        String name,
        DatabaseConnection conn
    ) {
        write(props, "spring.datasource." + name + ".", conn);
    }

    private void write(
        Properties props,
        String prefix,
        DatabaseConnection conn
    ) {
        if (conn.getUrl() != null) {
            props.setProperty(prefix + "url", conn.getUrl());
        }
        if (conn.getUsername() != null) {
            props.setProperty(prefix + "username", conn.getUsername());
        }
        if (conn.getPassword() != null) {
            props.setProperty(prefix + "password", conn.getPassword());
        }
    }

    private void removeDefault(Properties props) {
        removeByRegex(props, DEFAULT_KEY);
    }

    private void removeNamed(Properties props, String name) {
        Pattern p = Pattern.compile(
            "^spring\\.datasource\\." + Pattern.quote(name) +
            "\\.(url|username|password)$"
        );
        removeByRegex(props, p);
    }

    private void backupDefault(Properties props, String backupName) {
        Properties copy = new Properties();

        props.forEach((k, v) -> {
            String key = k.toString();
            if (DEFAULT_KEY.matcher(key).matches()) {
                String field = key.substring(key.lastIndexOf('.') + 1);
                copy.setProperty(
                    "spring.datasource." + backupName + "." + field,
                    v.toString()
                );
            }
        });

        props.putAll(copy);
    }

    private boolean hasNamed(Properties props, String name) {
        Pattern p = Pattern.compile(
            "^spring\\.datasource\\." + Pattern.quote(name) + "\\."
        );
        return props.keySet().stream()
            .map(Object::toString)
            .anyMatch(k -> p.matcher(k).find());
    }

    private DatabaseConnection extractNamed(
        Properties props,
        String name
    ) {
        DatabaseConnection conn = new DatabaseConnection();
        Pattern p = Pattern.compile(
            "^spring\\.datasource\\." + Pattern.quote(name) +
            "\\.(url|username|password)$"
        );

        props.forEach((k, v) -> {
            Matcher m = p.matcher(k.toString());
            if (m.matches()) {
                populate(conn, m.group(1), v.toString());
            }
        });

        return conn;
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

    private void removeByRegex(Properties props, Pattern pattern) {
        Iterator<Object> it = props.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (pattern.matcher(key).matches()) {
                it.remove();
            }
        }
    }
}