package io.github.mdasifmustafa.sbx.runtime;

import java.nio.file.Files;
import java.nio.file.Path;

public class BuildToolDetector {

    public static String detect(Path root) {
        if (Files.exists(root.resolve("mvnw"))) return "maven";
        if (Files.exists(root.resolve("gradlew"))) return "gradle";
        if (Files.exists(root.resolve("pom.xml"))) return "maven";
        if (Files.exists(root.resolve("build.gradle")) || Files.exists(root.resolve("build.gradle.kts")))
            return "gradle";

        throw new IllegalStateException("Unable to detect build tool");
    }
}