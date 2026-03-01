package io.github.mdasifmustafa.sbx.command.config.db;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.command.DependencyAddCommand;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConfigResolver;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConfigWriter;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConnection;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConnectionFactory;
import io.github.mdasifmustafa.sbx.config.db.DatabaseEngine;
import io.github.mdasifmustafa.sbx.config.db.DatabaseEngineRegistry;
import io.github.mdasifmustafa.sbx.config.db.DatabasePropertiesWriter;
import io.github.mdasifmustafa.sbx.config.db.HibernateDialectRegistry;
import io.github.mdasifmustafa.sbx.config.db.SpringApplicationWriter;
import io.github.mdasifmustafa.sbx.dependency.DependencyEnsureService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "set",
        description = "Add or update a database connection"
)
public class ConfigDbSetCommand implements Runnable {

    /* =========================
       Flags
       ========================= */

    @Option(names = "--profile", description = "Active profile (dev, test, etc)")
    String profile;

    @Option(names = "--engine", description = "Database engine key")
    String engineFlag;

    @Option(names = "--host")
    String hostFlag;

    @Option(names = "--port")
    Integer portFlag;

    @Option(names = "--database")
    String databaseFlag;

    @Option(names = "--username")
    String usernameFlag;

    @Option(names = "--password")
    String passwordFlag;

    @Option(names = "--file", description = "Database file (sqlite/h2)")
    String fileFlag;

    @Option(names = "--h2-mode", description = "H2 mode: file | mem")
    String h2ModeFlag;

    @Option(names = "--default", description = "Make default")
    boolean defaultFlag;

    /* ========================= */

    private String selectedEngineKey;

    /* ========================================================= */

    @Override
    public void run() {

        Scanner in = new Scanner(System.in);

        /* =========================
           Connection name
           ========================= */
        System.out.print("Connection name: ");
        String name = in.nextLine().trim();

        if (name.isEmpty()) {
            throw new IllegalStateException(
                    "Connection name cannot be empty"
            );
        }

        /* =========================
           Resolve config file
           ========================= */
        DatabaseConfigResolver resolver =
                new DatabaseConfigResolver();

        DatabaseConfigResolver.Resolution resolution =
                resolver.resolve(profile);

        /* =========================
           Prompt connection
           ========================= */
        DatabaseConnection conn =
                promptForConnection(in, resolution);

        /* =========================
           Default?
           ========================= */
        boolean makeDefault =
                defaultFlag || askYesNo(in, "Make it default", false);

        String backupName = null;

        if (resolution.getFormat()
                == DatabaseConfigResolver.Format.PROPERTIES
                && makeDefault) {

            System.out.println(
                    "You are about to replace the current default database connection."
            );

            System.out.print(
                    "Enter a name to save the existing default connection " +
                            "(leave empty to discard it): "
            );

            String input = in.nextLine().trim();
            if (!input.isEmpty()) {
                backupName = input;
            }
        }

        /* =========================
           Write DB config
           ========================= */
        if (resolution.getFormat()
                == DatabaseConfigResolver.Format.PROPERTIES) {

            DatabasePropertiesWriter writer =
                    new DatabasePropertiesWriter();

            if (makeDefault) {
                writer.promoteToDefault(
                        resolution.getFile(),
                        name,
                        conn,
                        backupName
                );
            } else {
                writer.set(
                        resolution.getFile(),
                        name,
                        conn
                );
            }

        } else {

            new DatabaseConfigWriter().set(
                    resolution.getFile(),
                    name,
                    conn,
                    makeDefault
            );
        }

        /* =========================
           Ensure Hibernate Dialect
           ========================= */
        ensureHibernateDialect(selectedEngineKey, resolution);

        /* =========================
           Feedback
           ========================= */
        System.out.println(
                "Connection '" + name + "' saved."
        );

        if (makeDefault) {
            System.out.println("Set as default.");
        }
    }

    /* =========================================================
       Connection Prompt
       ========================================================= */

    private DatabaseConnection promptForConnection(
            Scanner in,
            DatabaseConfigResolver.Resolution resolution
    ) {

        /* =========================
           Engine selection
           ========================= */

        String engineKey;

        if (engineFlag != null && !engineFlag.isBlank()) {
            engineKey = engineFlag;
        } else {

            Map<Integer, String> menu = new LinkedHashMap<>();
            int index = 1;

            System.out.println("Select database engine:");
            for (Map.Entry<String, DatabaseEngine> e
                    : DatabaseEngineRegistry.allEngines().entrySet()) {

                if (!e.getValue().enabledByDefault) continue;

                menu.put(index, e.getKey());

                System.out.println(
                        "  " + index + ") " +
                                e.getValue().displayName +
                                " (" + e.getKey() + ")"
                );
                index++;
            }

            System.out.print("Engine [1-" + (index - 1) + "]: ");

            int choice = Integer.parseInt(in.nextLine().trim());
            engineKey = menu.get(choice);
        }

        DatabaseEngine engine =
                DatabaseEngineRegistry.get(engineKey);

        if (engine == null) {
            throw new IllegalStateException(
                    "Unsupported engine: " + engineKey
            );
        }

        this.selectedEngineKey = engineKey;

        /* =========================
           Ensure JDBC dependency
           ========================= */
        if (engine.dependency != null) {
            DependencyEnsureService.ensure(
                    Path.of("."),
                    engine.dependency,
                    "runtime",
                    true
            );
        }

        /* =========================
           SQLITE
           ========================= */
        if (engineKey.equals("sqlite")) {

            String file = fileFlag != null
                    ? fileFlag
                    : promptWithDefault(in, "Database file", "data.db");

            return DatabaseConnectionFactory.forEngine(
                    engineKey,
                    engine,
                    null,
                    null,
                    file,
                    null,
                    null
            );
        }

        /* =========================
           H2
           ========================= */
        if (engineKey.equals("h2")) {

            String mode = h2ModeFlag;

            if (mode == null) {
                System.out.println("Select H2 mode:");
                System.out.println("  1) File");
                System.out.println("  2) Memory");

                System.out.print("Mode [1-2]: ");
                String input = in.nextLine().trim();
                mode = "2".equals(input) ? "mem" : "file";
            }

            String dbName = databaseFlag != null
                    ? databaseFlag
                    : promptWithDefault(in, "Database name", "testdb");

            String path =
                    "mem".equalsIgnoreCase(mode)
                            ? "mem:" + dbName
                            : "file:./" + dbName;

            return DatabaseConnectionFactory.forEngine(
                    engineKey,
                    engine,
                    null,
                    null,
                    path,
                    "sa",
                    ""
            );
        }

        /* =========================
           SERVER DATABASES
           ========================= */

        String host = hostFlag != null
                ? hostFlag
                : promptWithDefault(
                        in,
                        "Host",
                        DatabaseEngineRegistry.defaultHost()
                );

        int port = portFlag != null
                ? portFlag
                : Integer.parseInt(
                        promptWithDefault(
                                in,
                                "Port",
                                String.valueOf(engine.defaultPort)
                        )
                );

        String database = databaseFlag != null
                ? databaseFlag
                : promptWithDefault(
                        in,
                        "Database",
                        DatabaseEngineRegistry.defaultDatabase()
                );

        String username = usernameFlag != null
                ? usernameFlag
                : promptWithDefault(
                        in,
                        "Username",
                        DatabaseEngineRegistry.defaultUsername()
                );

        String password = passwordFlag != null
                ? passwordFlag
                : promptPassword(in);

        return DatabaseConnectionFactory.forEngine(
                engineKey,
                engine,
                host,
                port,
                database,
                username,
                password
        );
    }

    /* =========================================================
       Hibernate Dialect Handling
       ========================================================= */

    private void ensureHibernateDialect(
            String engineKey,
            DatabaseConfigResolver.Resolution resolution
    ) {

        String dialect =
                HibernateDialectRegistry.getDialect(engineKey);

        if (dialect == null) return;

        /* SQLite needs extra dependency */
        if (engineKey.equals("sqlite")) {

            new picocli.CommandLine(
                    new DependencyAddCommand()
            ).execute(
                    "org.hibernate.orm:hibernate-community-dialects",
                    "--confirm"
            );
        }

        new SpringApplicationWriter()
                .setJpaDialect(resolution, dialect);
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private String promptWithDefault(
            Scanner in,
            String label,
            String defaultValue
    ) {
        System.out.print(
                label + " [" + defaultValue + "]: "
        );

        String input = in.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private String promptPassword(Scanner in) {
        System.out.print("Password: ");
        return in.nextLine();
    }

    private boolean askYesNo(
            Scanner in,
            String label,
            boolean defaultValue
    ) {
        String suffix =
                defaultValue ? " (Y/n): " : " (y/N): ";

        System.out.print(label + suffix);

        String input = in.nextLine().trim().toLowerCase();

        if (input.isEmpty()) return defaultValue;

        return input.startsWith("y");
    }
}