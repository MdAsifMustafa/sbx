package io.github.mdasifmustafa.sbx.command.config.db;

import io.github.mdasifmustafa.sbx.config.db.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "rename",
    description = "Rename a database connection"
)
public class ConfigDbRenameCommand implements Runnable {

    @Parameters(index = "0", description = "Old connection name")
    String oldName;

    @Parameters(index = "1", description = "New connection name")
    String newName;

    @Option(
        names = "--profile",
        description = "Active profile"
    )
    String profile;

    @Override
    public void run() {
        DatabaseConfigResolver.Resolution res =
            new DatabaseConfigResolver().resolve(profile);

        if (res.getFormat() == DatabaseConfigResolver.Format.YAML) {

            DatabaseConfig cfg =
                new DatabaseConfigLoader().load(res.getFile());

            DatabaseConnection conn =
                cfg.getConnections().get(oldName);

            if (conn == null) {
                System.err.println(
                    "Connection '" + oldName + "' does not exist."
                );
                return;
            }

            DatabaseConfigWriter writer =
                new DatabaseConfigWriter();

            writer.set(res.getFile(), newName, conn, false);
            writer.remove(res.getFile(), oldName);

        } else {
            // PROPERTIES (regex-based)
            new DatabasePropertiesWriter()
                .rename(res.getFile(), oldName, newName);
        }

        System.out.println(
            "✔ Renamed connection '" + oldName +
            "' → '" + newName + "'"
        );
    }
}