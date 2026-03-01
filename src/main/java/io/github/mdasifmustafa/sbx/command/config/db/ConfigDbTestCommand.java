package io.github.mdasifmustafa.sbx.command.config.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.mdasifmustafa.sbx.config.db.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "test",
    description = "Test a database connection"
)
public class ConfigDbTestCommand implements Runnable {

    @Parameters(
        arity = "0..1",
        description = "Connection name (default if omitted)"
    )
    String name;

    @Option(
        names = "--profile",
        description = "Active profile"
    )
    String profile;

    @Override
    public void run() {
        DatabaseConfigResolver.Resolution res =
            new DatabaseConfigResolver().resolve(profile);

        DatabaseConfig cfg =
            res.getFormat() == DatabaseConfigResolver.Format.YAML
                ? new DatabaseConfigLoader().load(res.getFile())
                : new DatabasePropertiesReader().read(res.getFile());

        String target =
            name != null ? name : cfg.getDefaultConnection();

        DatabaseConnection conn =
            cfg.getConnections().get(target);

        if (conn == null) {
            System.err.println(
                "Connection '" + target + "' does not exist."
            );
            return;
        }

        DatabaseEngine engine =
            DatabaseEngineRegistry.get(conn.getEngine());

        if (engine == null) {
            System.err.println(
                "Unsupported database engine: " + conn.getEngine()
            );
            return;
        }

        if (engine.driverClass != null) {
            try {
                Class.forName(engine.driverClass);
            } catch (ClassNotFoundException e) {
                System.err.println(
                    "✖ JDBC driver not found.\n" +
                    "  Add dependency: " + engine.dependency
                );
                return;
            }
        }

        String url =
            engine.jdbcPrefix +
            conn.getHost() + ":" +
            conn.getPort() + "/" +
            conn.getDatabase();

        System.out.println(
            "Testing connection '" + target + "'..."
        );

        try (Connection c =
                 DriverManager.getConnection(
                     url,
                     conn.getUsername(),
                     conn.getPassword()
                 )) {

            System.out.println("✔ Connection successful.");

        } catch (SQLException e) {
            System.err.println(
                "✖ Connection failed: " + e.getMessage()
            );
        }
    }
}