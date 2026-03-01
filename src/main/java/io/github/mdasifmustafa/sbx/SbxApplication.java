package io.github.mdasifmustafa.sbx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mdasifmustafa.sbx.command.AppCommand;
import io.github.mdasifmustafa.sbx.command.BuildCommand;
import io.github.mdasifmustafa.sbx.command.CleanCommand;
import io.github.mdasifmustafa.sbx.command.ConfigCommand;
import io.github.mdasifmustafa.sbx.command.ConvertCommand;
import io.github.mdasifmustafa.sbx.command.DependencyCommand;
import io.github.mdasifmustafa.sbx.command.DoctorCommand;
import io.github.mdasifmustafa.sbx.command.LogCommand;
import io.github.mdasifmustafa.sbx.command.MakeCommand;
import io.github.mdasifmustafa.sbx.command.RestartCommand;
import io.github.mdasifmustafa.sbx.command.StartCommand;
import io.github.mdasifmustafa.sbx.command.StatusCommand;
import io.github.mdasifmustafa.sbx.command.StopCommand;
import io.github.mdasifmustafa.sbx.error.SbxException;
import io.github.mdasifmustafa.sbx.runtime.AppInfo;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "sbx",
        description = "SBX (Spring Boot eXperience) CLI",
        version = "SBX 1.0.0 - Spring Boot eXperience CLI by asifmustafamd",
        mixinStandardHelpOptions = true,
        subcommands = {
                AppCommand.class,
                BuildCommand.class,
                CleanCommand.class,
                StartCommand.class,
                StopCommand.class,
                StatusCommand.class,
                LogCommand.class,
                RestartCommand.class,
                DoctorCommand.class,
                DependencyCommand.class,
                MakeCommand.class,
                ConfigCommand.class,
                ConvertCommand.class
        }
)
public class SbxApplication implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SbxApplication.class);

    @Override
    public void run() {
        AppInfo.printBanner();
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        try {
            int exitCode = new CommandLine(new SbxApplication()).execute(args);
            System.exit(exitCode);
        } catch (SbxException e) {
            logger.error("SbxException occurred", e);
            System.err.println("✖ " + e.getMessage());
            System.exit(e.getExitCode());
        } catch (RuntimeException e) {
            logger.error("Unexpected runtime error", e);
            System.err.println("✖ Unexpected error: " + e.getMessage());
            System.exit(99);
        }
    }
}