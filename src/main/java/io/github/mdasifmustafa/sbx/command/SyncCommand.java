package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Path;
import java.util.Map;

import io.github.mdasifmustafa.sbx.build.DependencyApplyService;
import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.runtime.BuildTool;
import io.github.mdasifmustafa.sbx.runtime.BuildToolResolver;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "sync",
        description = "Synchronize build files with sbx.json"
)
public class SyncCommand implements Runnable {

    @Option(
            names = "--format",
            description = "Format build file after sync"
    )
    boolean format;

    @Option(
            names = "--dry-run",
            description = "Show what would change without applying"
    )
    boolean dryRun;

    @Override
    public void run() {

        Path root = Path.of(".");
        Path configFile = Path.of("sbx.json");

        SbxConfig config = SbxConfigReader.read(configFile);

        if (config.getDependencies() == null ||
            config.getDependencies().isEmpty()) {

            System.out.println("No dependencies to sync.");
            return;
        }

        BuildTool tool = resolveBuildTool(config);

        System.out.println("Syncing project using " + tool.name().toLowerCase());

        int applied = 0;

        for (Map.Entry<String, DependencyConfig> entry :
                config.getDependencies().entrySet()) {

            String coords = entry.getKey();
            var dep = entry.getValue();

            if (dryRun) {
                System.out.println("• Would apply: " + coords);
                continue;
            }

            DependencyApplyService.apply(
                    root,
                    coords,
                    dep,
                    false // format later once
            );

            applied++;
        }

        if (dryRun) {
            System.out.println("Dry run complete.");
            return;
        }

        if (format) {
            runFormatter(root);
        }

        System.out.println("✔ Synced " + applied + " dependencies.");
    }

    // ======================================================
    // TOOL RESOLUTION
    // ======================================================

    private BuildTool resolveBuildTool(SbxConfig config) {

        String value = config.getRuntime().getBuild();

        if ("maven".equalsIgnoreCase(value)) {
            return BuildTool.MAVEN;
        }

        if ("gradle".equalsIgnoreCase(value)) {
            return BuildTool.GRADLE;
        }

        throw new IllegalStateException(
                "Unsupported build tool: " + value);
    }

    // ======================================================
    // FORMATTER
    // ======================================================

    private void runFormatter(Path root) {

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    BuildToolResolver.resolve(BuildTool.MAVEN),
                    "-q",
                    "spotless:apply"
            );

            pb.directory(root.toFile());
            pb.inheritIO();

            Process p = pb.start();
            int exit = p.waitFor();

            if (exit != 0) {

                ProcessBuilder pb2 = new ProcessBuilder(
                        BuildToolResolver.resolve(BuildTool.MAVEN),
                        "-q",
                        "formatter:format"
                );

                pb2.directory(root.toFile());
                pb2.inheritIO();
                pb2.start().waitFor();
            }

        } catch (Exception ignored) {
            System.out.println("⚠ Formatter not available.");
        }
    }
}