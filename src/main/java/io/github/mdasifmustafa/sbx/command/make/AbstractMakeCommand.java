package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import io.github.mdasifmustafa.sbx.runtime.ProjectPackageResolver;

public abstract class AbstractMakeCommand implements Runnable {

    protected boolean ensureProject() {
        if (!Files.exists(Path.of("sbx.json"))) {
            System.err.println("❌ sbx.json not found. Are you in an SBX project?");
            return false;
        }
        return true;
    }

    /**
     * Normalize class name by appending suffix if missing.
     * Example: User + Controller -> UserController
     */
    protected String normalize(String name, String suffix) {
        return name.endsWith(suffix) ? name : name + suffix;
    }

    /**
     * Parse names like:
     *   User
     *   UserController
     *   admin/api/User
     *
     * Into:
     *   className = UserController
     *   subPackage = admin.api
     */
    protected NameParts parseName(String rawName, String suffix) {
        String normalized = rawName.replace("\\", "/");
        String[] segments = normalized.split("/");

        String rawClassName = segments[segments.length - 1];
        String className = normalize(rawClassName, suffix);

        String subPackage = "";
        if (segments.length > 1) {
            subPackage = String.join(
                    ".",
                    Arrays.copyOf(segments, segments.length - 1)
            );
        }

        return new NameParts(className, subPackage);
    }

    /**
     * Resolve the project base package.
     *
     * <p>Currently uses ProjectPackageResolver to detect package from source files.
     * Future enhancement: read from sbx.json or scan @SpringBootApplication annotation.
     */
    protected String resolveBasePackage() {
        return ProjectPackageResolver.resolveBasePackage();
    }

    /**
     * Resolve Java file path based on full package name.
     */
    protected Path resolveJavaPath(String packageName, String className) {
        return Path.of(
                "src/main/java",
                packageName.replace(".", "/"),
                className + ".java"
        );
    }

    protected void write(Path path, String content, boolean force, boolean dryRun) {
        try {
            if (dryRun) {
                System.out.println("📝 Would create " + path);
                System.out.println(content);
                return;
            }

            if (Files.exists(path) && !force) {
                System.err.println("❌ File already exists: " + path + " (use --force)");
                return;
            }

            Files.createDirectories(path.getParent());
            Files.writeString(path, content);

            System.out.println(force ? "♻️  Overwritten " + path : "✅ Created " + path);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    /**
     * Value object for parsed names.
     */
    protected record NameParts(String className, String subPackage) {
    }
}