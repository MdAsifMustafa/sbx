package io.github.mdasifmustafa.sbx.command;

import picocli.CommandLine.Command;

@Command(
        name = "dependency",
        description = "Manage project dependencies",
        subcommands = {
                DependencyAddCommand.class,
                DependencyRemoveCommand.class,
                SyncCommand.class
        }
)
public class DependencyCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Usage: sbx dependency <add|remove|list>");
    }
}