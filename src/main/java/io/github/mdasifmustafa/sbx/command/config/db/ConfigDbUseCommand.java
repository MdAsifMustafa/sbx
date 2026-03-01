package io.github.mdasifmustafa.sbx.command.config.db;

import java.util.Scanner;

import io.github.mdasifmustafa.sbx.config.db.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "use",
    description = "Make a database connection the default"
)
public class ConfigDbUseCommand implements Runnable {
	
	Scanner IN = new Scanner(System.in);

    @Parameters(index = "0", description = "Connection name")
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

        if (res.getFormat() == DatabaseConfigResolver.Format.PROPERTIES) {

            System.out.println(
                "You are about to replace the current default database connection."
            );
            System.out.print(
                "Enter a name to save the existing default connection " +
                "(leave empty to discard it): "
            );

            String backupName = IN.nextLine().trim();
            if (backupName.isEmpty()) {
                backupName = null;
            }

            new DatabasePropertiesWriter()
                .useWithBackup(res.getFile(), name, backupName);

        } else {
            // YAML: simple default switch
            new DatabaseConfigWriter()
                .use(res.getFile(), name);
        }

        System.out.println(
            "Connection '" + name + "' set as default."
        );
    }
}