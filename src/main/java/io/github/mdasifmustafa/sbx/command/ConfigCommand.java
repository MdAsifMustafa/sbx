package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.command.config.ConfigDbCommand;
import picocli.CommandLine.Command;

@Command(
        name = "config",
        description = "Manage application configuration",
        subcommands = {
        		ConfigDbCommand.class
        }
)
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use sbx config <module>");
    }
}
