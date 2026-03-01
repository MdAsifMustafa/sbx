package io.github.mdasifmustafa.sbx.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.runtime.AppRuntimeInfo;
import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import io.github.mdasifmustafa.sbx.runtime.BuildTool;
import io.github.mdasifmustafa.sbx.runtime.BuildToolResolver;
import io.github.mdasifmustafa.sbx.runtime.RuntimeState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "start",
        description = "Start the Spring Boot application"
)
public class StartCommand implements Runnable {

    @Option(
            names = "--profile",
            description = "Spring profile to activate"
    )
    private String profile;

    @Option(
            names = "--host",
            description = "Server bind address"
    )
    private String host;

    @Option(
            names = "--port",
            description = "Server port"
    )
    private Integer port;

    @Option(
            names = "--debug",
            description = "Enable Spring Boot debug mode"
    )
    private boolean debug;

    @Option(
            names = "--daemon",
            description = "Run application in background"
    )
    private boolean daemon;

    @Override
    public void run() {
        Path configPath = Path.of("sbx.json");

        if (!Files.exists(configPath)) {
            System.err.println("❌ sbx.json not found. Are you in an SBX project?");
            return;
        }

        var config = SbxConfigReader.read(configPath);
        String build = config.getRuntime().getBuild();

        BuildTool tool = resolveBuildTool(build);
        String executable = BuildToolResolver.resolve(tool);

        List<String> command = new ArrayList<>();
        command.add(executable);

        switch (tool) {
            case MAVEN -> buildMavenCommand(command);
            case GRADLE -> buildGradleCommand(command);
        }
        

        System.out.println("🚀 Starting application" + (daemon ? " in daemon mode" : ""));
        BuildExecutor.run(command, daemon);
        if (daemon) {
            try {
                RuntimeState.write(new AppRuntimeInfo(
                    BuildExecutor.readPid(),
                    host != null ? host : "localhost",
                    port != null ? port : 8080,
                    "/actuator/health"
                ));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write runtime state", e);
            }
        }
    }

    /* ===================== build commands ===================== */

    private void buildMavenCommand(List<String> command) {
        command.add("spring-boot:run");

        if (profile != null && !profile.isBlank()) {
            command.add("-Dspring-boot.run.profiles=" + profile);
        }

        String args = buildServerArgs();
        if (!args.isBlank()) {
            command.add("-Dspring-boot.run.arguments=" + args);
        }
    }

    private void buildGradleCommand(List<String> command) {
        command.add("bootRun");

        String args = buildServerArgs();
        if (!args.isBlank()) {
            command.add("--args=" + args);
        }
    }

    /* ===================== helpers ===================== */

    private String buildServerArgs() {
        List<String> args = new ArrayList<>();

        if (host != null) {
            args.add("--server.address=" + host);
        }

        if (port != null) {
            args.add("--server.port=" + port);
        }

        if (debug) {
            args.add("--debug");
        }

        return String.join(" ", args);
    }

    private BuildTool resolveBuildTool(String value) {
        if ("maven".contains(value)) {
            return BuildTool.MAVEN;
        }
        if ("gradle".contains(value)) {
            return BuildTool.GRADLE;
        }
        throw new IllegalStateException("Unsupported build tool: " + value);
    }
}