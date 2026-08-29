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
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

@Command(
        name = "sbx",
        description = "SBX (Spring Boot eXperience) CLI",
        versionProvider = SbxApplication.AppVersionProvider.class,
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

    static class AppVersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[] { AppInfo.getDisplayVersion() };
        }
    }

    @Override
    public void run() {
        AppInfo.printBanner();
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new SbxApplication());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setExecutionExceptionHandler((ex, cmd, parseResult) -> {
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            SbxResponse.error(message);
            return 1;
        });

        try {
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } catch (SbxException e) {
            logger.error("SbxException occurred", e);
            SbxResponse.error(e.getMessage());
            System.exit(e.getExitCode());
        } catch (RuntimeException e) {
            String message = "Unexpected error: " + e.getMessage();
            logger.error(message, e);
            SbxResponse.error(message);
            System.exit(99);
        }
    }
}