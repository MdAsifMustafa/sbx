package io.github.mdasifmustafa.sbx.command.config.db;

import io.github.mdasifmustafa.sbx.config.db.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "validate",
    description = "Validate database configuration"
)
public class ConfigDbValidateCommand implements Runnable {

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

        if (cfg.getConnections().isEmpty()) {
            System.err.println("No database connections found.");
            return;
        }

        if (!cfg.getConnections()
                .containsKey(cfg.getDefaultConnection())) {
            System.err.println(
                "Default connection '" +
                cfg.getDefaultConnection() +
                "' does not exist."
            );
            return;
        }

        System.out.println("✔ Database configuration is valid.");
    }
}