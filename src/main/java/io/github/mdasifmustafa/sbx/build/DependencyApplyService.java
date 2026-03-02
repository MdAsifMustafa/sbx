package io.github.mdasifmustafa.sbx.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;

public class DependencyApplyService {

    private static final List<DependencyApplier> APPLIERS = List.of(
            new MavenDependencyApplier(),
            new GradleDependencyApplier()
    );

    public static void apply(Path root,
            String coords,
            DependencyConfig dep,
            boolean format) {

        for (DependencyApplier applier : APPLIERS) {

            if (!applier.supports(root)) {
                continue;
            }

            Path buildFile = resolveBuildFile(root);

            String originalContent = null;

            try {
                originalContent = Files.readString(buildFile);

                applier.apply(root, coords, dep);
                
                if (dep.annotationProcessor) {

                    String[] parts = coords.split(":");

                    String groupId = parts[0];
                    String artifactId = parts[1];
                    String version = dep.version;

                    String processorArtifact =
                            dep.processorArtifact != null
                                    ? dep.processorArtifact
                                    : artifactId;

                    String processorCoords =
                            groupId + ":" + processorArtifact +
                            (version != null ? ":" + version : "");

                    DependencyConfig processorConfig =
                            new DependencyConfig();

                    processorConfig.version = version;
                    processorConfig.scope = "annotationProcessor";
                    processorConfig.annotationProcessor = true;

                    applier.apply(root, processorCoords, processorConfig);
                }
                
                if (format) {
                    runFormatter(root);
                }

                return;

            } catch (Exception e) {

                // Rollback
                if (originalContent != null) {
                    try {
                        Files.writeString(buildFile, originalContent);
                    } catch (Exception ignored) {}
                }

                throw new IllegalStateException(
                        "Dependency application failed. Rolled back.",
                        e
                );
            }
        }

        throw new IllegalStateException("No supported build tool found");
    }
    private static void runFormatter(Path root) {

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "mvn", "-q", "spotless:apply"
            );

            pb.directory(root.toFile());
            pb.inheritIO();

            Process p = pb.start();
            int exit = p.waitFor();

            if (exit != 0) {
                // fallback
                ProcessBuilder pb2 = new ProcessBuilder(
                        "mvn", "-q", "formatter:format"
                );

                pb2.directory(root.toFile());
                pb2.inheritIO();
                pb2.start().waitFor();
            }

        } catch (Exception ignored) {
            // formatter optional
        }
    }

    public static void remove(Path root, String coords) {

        for (DependencyApplier applier : APPLIERS) {
            if (applier.supports(root)) {
                try {
                    applier.remove(root, coords);
                    return;
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to remove dependency: " + coords, e
                    );
                }
            }
        }

        throw new IllegalStateException("No supported build tool found");
    }

    private static Path resolveBuildFile(Path root) {
        if (Files.exists(root.resolve("pom.xml")))
            return root.resolve("pom.xml");

        if (Files.exists(root.resolve("build.gradle")))
            return root.resolve("build.gradle");

        if (Files.exists(root.resolve("build.gradle.kts")))
            return root.resolve("build.gradle.kts");

        throw new IllegalStateException("No build file found");
    }
}