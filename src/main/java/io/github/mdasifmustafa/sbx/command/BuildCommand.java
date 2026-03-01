package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.mdasifmustafa.sbx.build.DependencyApplyService;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import io.github.mdasifmustafa.sbx.runtime.BuildTool;
import io.github.mdasifmustafa.sbx.runtime.BuildToolResolver;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "build",
        description = "Build the project"
)
public class BuildCommand implements Runnable {

    @Option(
            names = "--skip-tests",
            description = "Skip running tests"
    )
    boolean skipTests;

    @Option(
            names = "--profile",
            description = "Spring profile to use during build (e.g. prod)"
    )
    String profile;

    @Option(
            names = "--sync",
            description = "Sync dependencies before build"
    )
    boolean sync;

    @Override
    public void run() {

        var config = SbxConfigReader.read(Path.of("sbx.json"));

        BuildTool tool = resolveBuildTool(config.getRuntime().getBuild());

        // ----------------------------------
        // Optional dependency sync
        // ----------------------------------

        if (sync && config.getDependencies() != null) {

            for (var entry : config.getDependencies().entrySet()) {

                DependencyApplyService.apply(
                        Path.of("."),
                        entry.getKey(),
                        entry.getValue(),
                        false
                );
            }
        }

        // ----------------------------------
        // Build command
        // ----------------------------------

        List<String> command = new ArrayList<>();

        command.add(BuildToolResolver.resolve(tool));

        switch (tool) {
            case MAVEN -> buildMaven(command);
            case GRADLE -> buildGradle(command);
        }

        BuildExecutor.run(command);
    }

    /* ===================== build logic ===================== */

    private void buildMaven(List<String> command) {

        command.add("clean");
        command.add("package");

        if (skipTests) {
            command.add("-DskipTests");
        }

        if (profile != null && !profile.isBlank()) {
            command.add("-P" + profile);
        }
    }

    private void buildGradle(List<String> command) {

        command.add("build");

        if (skipTests) {
            command.add("-x");
            command.add("test");
        }

        if (profile != null && !profile.isBlank()) {
            command.add("-PspringProfiles=" + profile);
        }
    }

    private BuildTool resolveBuildTool(String value) {

        if ("maven".equalsIgnoreCase(value)) {
            return BuildTool.MAVEN;
        }

        if ("gradle".equalsIgnoreCase(value)) {
            return BuildTool.GRADLE;
        }

        throw new IllegalStateException("Unsupported build tool: " + value);
    }
}