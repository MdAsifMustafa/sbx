package io.github.mdasifmustafa.sbx.command.config.db;

import io.github.mdasifmustafa.sbx.config.db.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "remove",
    description = "Remove a database connection"
)
public class ConfigDbRemoveCommand implements Runnable {

    @Parameters(index = "0", description = "Connection name")
    String name;

    @Option(names = "--profile")
    String profile;

    @Override
    public void run() {
        DatabaseConfigResolver.Resolution res =
            new DatabaseConfigResolver().resolve(profile);

        if (res.getFormat() == DatabaseConfigResolver.Format.PROPERTIES) {
            new DatabasePropertiesWriter()
                .remove(res.getFile(), name);
        } else {
            new DatabaseConfigWriter()
                .remove(res.getFile(), name);
        }

        System.out.println(
            "Connection '" + name + "' removed."
        );
    }
}