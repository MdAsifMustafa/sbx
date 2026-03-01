package io.github.mdasifmustafa.sbx.command.config.db;

import io.github.mdasifmustafa.sbx.config.db.DatabaseConfig;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConfigLoader;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConfigResolver;
import io.github.mdasifmustafa.sbx.config.db.DatabaseConfigResolver.Resolution;
import io.github.mdasifmustafa.sbx.config.db.DatabasePropertiesReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "list",
    description = "List configured database connections"
)
public class ConfigDbListCommand implements Runnable {

    @Option(
        names = "--profile",
        description = "Active profile (dev, test, etc)"
    )
    String profile;

    @Override
    public void run() {
    	DatabaseConfigResolver resolver =
    		    new DatabaseConfigResolver();

    		Resolution resolution =
    		    resolver.resolve(profile);

    		DatabaseConfig config;

    		if (resolution.getFormat()
    		    == DatabaseConfigResolver.Format.PROPERTIES) {

    		    DatabasePropertiesReader reader =
    		        new DatabasePropertiesReader();

    		    config = reader.read(resolution.getFile());

    		} else {

    		    DatabaseConfigLoader loader =
    		        new DatabaseConfigLoader();

    		    config = loader.load(resolution.getFile());
    		}
        printConfig(resolution, config);
    }

    private void printConfig(Resolution resolution,DatabaseConfig config) {
    	 System.out.println("Source : " + resolution.getFile());
         System.out.println("Format : " + resolution.getFormat());
        System.out.println("Default connection : " + config.getDefaultConnection());
        System.out.println("Connections:");

        config.getConnections().forEach((name, conn) -> {
            System.out.println("  - " + name);
            if (conn.getUrl() != null) {
                System.out.println("      url      : " + conn.getUrl());
            }
            if (conn.getEngine() != null) {
                System.out.println("      engine   : " + conn.getEngine());
            }
            if (conn.getHost() != null) {
                System.out.println("      host     : " + conn.getHost());
            }
            if (conn.getPort() != null) {
                System.out.println("      port     : " + conn.getPort());
            }
            if (conn.getDatabase() != null) {
                System.out.println("      database : " + conn.getDatabase());
            }
            if (conn.getUsername() != null) {
                System.out.println("      username : " + conn.getUsername());
            }
        });
    }
}