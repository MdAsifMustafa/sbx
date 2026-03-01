package io.github.mdasifmustafa.sbx.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class ProjectPackageResolver {

    private ProjectPackageResolver() {
    }

    public static String resolveBasePackage() {
        Path srcMainJava = Path.of("src/main/java");

        if (!Files.exists(srcMainJava)) {
            throw new IllegalStateException("src/main/java not found");
        }

        try (Stream<Path> files = Files.walk(srcMainJava)) {
            return files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(ProjectPackageResolver::containsSpringBootApplication)
                    .map(ProjectPackageResolver::extractPackage)
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Could not find @SpringBootApplication class"
                            ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan source files", e);
        }
    }

    private static boolean containsSpringBootApplication(Path file) {
        try {
            return Files.readString(file)
                    .contains("@SpringBootApplication");
        } catch (IOException e) {
            return false;
        }
    }

    private static String extractPackage(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);

            return lines.stream()
                    .filter(line -> line.startsWith("package "))
                    .findFirst()
                    .map(line -> line
                            .replace("package", "")
                            .replace(";", "")
                            .trim()
                    )
                    .orElseThrow();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read package from " + file,
                    e
            );
        }
    }
}