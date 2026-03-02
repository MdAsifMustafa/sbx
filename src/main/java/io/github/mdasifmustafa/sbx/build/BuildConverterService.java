package io.github.mdasifmustafa.sbx.build;

import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import io.github.mdasifmustafa.sbx.ux.Console;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BuildConverterService {

	public static void convert(Path root,
            SbxConfig config,
            String target,
            String dsl,
            boolean wrapper,
            boolean sync) {

        try {

            backupExisting(root);

            // update runtime
            config.getRuntime().setBuild(target.toLowerCase());

            BuildFileGenerator.generate(root, config, target, dsl);

         // sync dependencies
         if (sync && config.getDependencies() != null) {
             for (var entry : config.getDependencies().entrySet()) {
                 DependencyApplyService.apply(
                         root,
                         entry.getKey(),
                         entry.getValue(),
                         true
                 );
             }
         }

         // wrapper
         if (wrapper) {
             generateWrapper(root, target);
         }

            Console.success("Build tool converted to " + target);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to convert build tool", e);
        }
    }

    // ======================================================
    // BACKUP
    // ======================================================

    private static void backupExisting(Path root) throws Exception {

        backup(root.resolve("pom.xml"));
        backup(root.resolve("build.gradle"));
        backup(root.resolve("build.gradle.kts"));
    }

    private static void backup(Path file) throws Exception {

        if (!Files.exists(file)) return;

        Path backup = file.resolveSibling(
                file.getFileName().toString() + "_backup"
        );

        Files.move(file, backup);

        Console.success("Backed up " + file.getFileName()
                + " → " + backup.getFileName());
    }

    // ======================================================
    // WRAPPER
    // ======================================================

    private static void generateWrapper(Path root,
                                        String target) {

        try {

            Console.info("Generating " + target + " wrapper…");

            if (target.equalsIgnoreCase("gradle")) {
                BuildExecutor.run(List.of("gradle", "wrapper"));
            } else {
                BuildExecutor.run(List.of("mvn", "-N", "wrapper:wrapper"));
            }

        } catch (Exception e) {
            Console.warning("Wrapper generation skipped (tool not installed)");
        }
    }
}