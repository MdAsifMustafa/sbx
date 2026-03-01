package io.github.mdasifmustafa.sbx.config.db;

import java.util.Collections;
import java.util.Map;

public final class DatabaseConfig {

    private final String defaultConnection;
    private final Map<String, DatabaseConnection> connections;

    public DatabaseConfig(
        String defaultConnection,
        Map<String, DatabaseConnection> connections
    ) {
        this.defaultConnection = defaultConnection;
        this.connections = Map.copyOf(connections);
    }

    public String getDefaultConnection() {
        return defaultConnection;
    }

    public Map<String, DatabaseConnection> getConnections() {
        return Collections.unmodifiableMap(connections);
    }
}