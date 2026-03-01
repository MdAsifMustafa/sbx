package io.github.mdasifmustafa.sbx.runtime;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.runtime.util.OsUtils;

public final class BuildToolResolver {

    private BuildToolResolver() {}

    public static String resolve(BuildTool tool) {
        return switch (tool) {
            case MAVEN -> resolveMaven();
            case GRADLE -> resolveGradle();
        };
    }

    private static String resolveMaven() {
        if (OsUtils.isWindows()) {
            if (Files.exists(Path.of("mvnw.cmd"))) {
                return "mvnw.cmd";
            }
        } else {
            if (Files.exists(Path.of("mvnw"))) {
                return "./mvnw";
            }
        }
        return "mvn";
    }

    private static String resolveGradle() {
        if (OsUtils.isWindows()) {
            if (Files.exists(Path.of("gradlew.bat"))) {
                return "gradlew.bat";
            }
            return "gradle.bat";
        } else {
            if (Files.exists(Path.of("gradlew"))) {
                return "./gradlew";
            }
            return "gradle";
        }
    }
}