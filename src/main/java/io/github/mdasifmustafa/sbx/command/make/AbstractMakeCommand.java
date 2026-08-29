package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import io.github.mdasifmustafa.sbx.runtime.ProjectPackageResolver;
import io.github.mdasifmustafa.sbx.dependency.DependencyManager;
import picocli.CommandLine.Option;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;

public abstract class AbstractMakeCommand implements Runnable {

    protected boolean ensureProject() {
        if (!Files.exists(Path.of("sbx.json"))) {
            SbxResponse.error("❌ sbx.json not found. Are you in an SBX project?");
            return false;
        }
        try {
            if (noDepsCheck) {
                SbxResponse.tip("Skipping dependency checks (--no-deps-check)");
            } else {
                if (autoApplyDeps) {
                    DependencyManager.ensureDependencies(true, requiredDependencies());
                } else {
                    DependencyManager.ensureDependencies(requiredDependencies());
                }

                if (depsOnly) {
                    SbxResponse.success("Dependency operations completed (--deps-only). Exiting.");
                    return false;
                }
            }
        } catch (Exception e) {
            SbxResponse.warning("Dependency check failed: " + e.getMessage());
        }

        return true;
    }

    /**
     * Override in subclasses to declare named dependency keys that should
     * be validated/installed before running the command.
     * By default no dependencies are checked.
     */
    protected String[] requiredDependencies() {
        return new String[0];
    }

    @Option(names = "--no-deps-check", description = "Skip dependency checks and prompts")
    private boolean noDepsCheck;

    @Option(names = "--auto-apply-deps", description = "Automatically add missing dependencies without prompting")
    private boolean autoApplyDeps;

    @Option(names = "--deps-only", description = "Only perform dependency checks/additions and exit before generation")
    private boolean depsOnly;

    // Protected accessors allow subclasses to propagate these flags when invoking nested commands
    protected boolean isNoDepsCheck() {
        return noDepsCheck;
    }

    protected boolean isAutoApplyDeps() {
        return autoApplyDeps;
    }

    protected boolean isDepsOnly() {
        return depsOnly;
    }

    /**
     * Normalize class name by appending suffix if missing.
     * Example: User + Controller -> UserController
     */
    protected String normalize(String name, String suffix) {
        String baseName = toTitleCase(name);
        return baseName.endsWith(suffix) ? baseName : baseName + suffix;
    }

    protected String toTitleCase(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String trimmed = name.trim();
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;

        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(ch));
                    capitalizeNext = false;
                } else {
                    sb.append(ch);
                }
            } else {
                capitalizeNext = true;
            }
        }

        return sb.toString();
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
            subPackage = normalizePackage(
                    String.join(".", Arrays.copyOf(segments, segments.length - 1))
            );
        }

        return new NameParts(className, subPackage);
    }

    protected boolean isSimpleName(String rawName) {
        if (rawName == null) {
            return false;
        }

        String trimmed = rawName.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains(".")) {
            return false;
        }

        return trimmed.matches("^[A-Za-z0-9_ ]+$");
    }

    protected String requireSimpleName(String rawName, String kind) {
        if (!isSimpleName(rawName)) {
            throw new IllegalArgumentException(
                    kind + " must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages."
            );
        }
        return rawName;
    }

    protected String normalizePackage(String packagePath) {
        if (packagePath == null || packagePath.isBlank()) {
            return "";
        }

        String normalized = packagePath.trim().replace('\\', '/');
        String[] segments = normalized.split("[/.]+");

        return Arrays.stream(segments)
                .filter(segment -> !segment.isBlank())
                .map(segment -> segment.toLowerCase(Locale.ROOT))
                .collect(Collectors.joining("."));
    }

    protected String resolveRelativePackage(String basePackage, String customPackage) {
        if (customPackage == null || customPackage.isBlank()) {
            return basePackage;
        }

        String normalized = normalizePackage(customPackage);
        return normalized.isBlank() ? basePackage : basePackage + "." + normalized;
    }

    protected String resolvePackage(String basePackage, String customPackage, String defaultSuffix) {
        if (customPackage == null || customPackage.isBlank()) {
            return defaultSuffix == null || defaultSuffix.isBlank()
                    ? basePackage
                    : basePackage + "." + defaultSuffix;
        }

        String normalized = normalizePackage(customPackage);
        String suffix = defaultSuffix == null || defaultSuffix.isBlank()
                ? ""
                : "." + defaultSuffix.replace(".", ".");

        return normalized.isBlank()
                ? (defaultSuffix == null || defaultSuffix.isBlank() ? basePackage : basePackage + "." + defaultSuffix)
                : basePackage + "." + normalized + suffix;
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
                SbxResponse.error("❌ File already exists: " + path + " (use --force)");
                return;
            }

            Files.createDirectories(path.getParent());
            Files.writeString(path, content);

            System.out.println(force ? "♻️  Overwritten " + path : "✅ Created " + path);

        } catch (Exception e) {
            SbxResponse.error("❌ " + e.getMessage());
        }
    }

    /**
     * Value object for parsed names.
     */
    protected record NameParts(String className, String subPackage) {
    }
}