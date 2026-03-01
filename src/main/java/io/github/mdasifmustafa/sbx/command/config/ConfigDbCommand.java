package io.github.mdasifmustafa.sbx.command.config;

import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbListCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbRemoveCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbRenameCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbSetCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbTestCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbUseCommand;
import io.github.mdasifmustafa.sbx.command.config.db.ConfigDbValidateCommand;
import picocli.CommandLine.Command;

@Command(
        name = "db",
        description = "DB configuration values",
        subcommands = {
        		ConfigDbListCommand.class,
        		ConfigDbSetCommand.class,
      		  	ConfigDbUseCommand.class, 
      		  	ConfigDbRemoveCommand.class,
      		  	ConfigDbValidateCommand.class,
      		  	ConfigDbTestCommand.class,
      		  	ConfigDbRenameCommand.class
        }
)
public class ConfigDbCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use sbx config set <module>");
    }
}