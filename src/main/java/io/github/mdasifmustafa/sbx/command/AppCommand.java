package io.github.mdasifmustafa.sbx.command;

import picocli.CommandLine.Command;

@Command(
        name = "app",
        description = "Manage SBX applications",
        subcommands = {
                AppNewCommand.class,
                AppInitCommand.class,
                
        }
)
public class AppCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Usage: sbx app <command>");
    }
}