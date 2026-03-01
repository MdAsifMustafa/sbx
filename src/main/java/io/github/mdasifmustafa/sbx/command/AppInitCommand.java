package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.config.ProjectConfig;
import io.github.mdasifmustafa.sbx.config.RuntimeConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.dependency.DependencyRuleRegistry;
import io.github.mdasifmustafa.sbx.dependency.MavenCentralValidator;
import io.github.mdasifmustafa.sbx.io.SbxConfigWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "init",
        description = "Initialize SBX in an existing Spring Boot project"
)
public class AppInitCommand implements Runnable {
	@Option(
	        names = "--boot-version",
	        description = "Spring Boot version override (e.g. 3.2.5 or 4.0.3.release)"
	)
	private String bootVersion;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        Path root = Path.of("").toAbsolutePath();

        if (Files.exists(root.resolve("sbx.json"))) {
            throw new IllegalStateException("sbx.json already exists in this project");
        }

        System.out.println("🔍 Detecting existing Spring Boot project...\n");

        String build = detectBuildTool(root);
        int java = detectJavaMajor();
        String springBoot = bootVersion != null && !bootVersion.isBlank()
                ? normalizeReleaseBootVersion(bootVersion)
                : normalizeReleaseBootVersion(detectSpringBootVersion(root));
        String configFormat = detectConfigFormat(root);
        Map<String, DependencyConfig> dependencies =
                detectDependencies(root, build);

        System.out.println("Detected configuration:");
        System.out.println("• Build tool    : " + build);
        System.out.println("• Java version  : " + java);
        System.out.println("• Spring Boot   : " + springBoot);
        System.out.println("• Config format : " + configFormat);
        System.out.println("• Dependencies  : " + dependencies.size());
        System.out.println();
        
        boolean auto = bootVersion != null;

        if (!auto && !confirm("Proceed with these values? (yes/no)")) {
            System.out.println("Initialization aborted.");
            return;
        }

        writeSbxConfig(
                root,
                root.getFileName().toString(),
                java,
                springBoot,
                build,
                configFormat,
                dependencies
        );

        System.out.println("\n✔ SBX initialized successfully");
    }

    // --------------------------------------------------
    // Detection
    // --------------------------------------------------

    private String detectBuildTool(Path root) {
        if (Files.exists(root.resolve("pom.xml"))) return "maven";
        if (Files.exists(root.resolve("build.gradle"))
                || Files.exists(root.resolve("build.gradle.kts"))) {
            return "gradle";
        }
        throw new IllegalStateException("Unable to detect build tool");
    }

    private int detectJavaMajor() {
        String version = System.getProperty("java.version");
        if (version == null) {
            return Integer.parseInt(ask("Java major version"));
        }
        return Integer.parseInt(version.split("\\.")[0]);
    }

    private String detectSpringBootVersion(Path root) {
        try {
            if (Files.exists(root.resolve("pom.xml"))) {
                String pom = Files.readString(root.resolve("pom.xml"));
                return extract(pom,
                        "<spring-boot.version>",
                        "</spring-boot.version>");
            }
        } catch (Exception ignored) {}
        return ask("Spring Boot version");
    }

    private String detectConfigFormat(Path root) {
        Path res = root.resolve("src/main/resources");
        if (Files.exists(res.resolve("application.yml"))) return "yaml";
        return "properties";
    }

    // --------------------------------------------------
    // Dependency detection
    // --------------------------------------------------

    private Map<String, DependencyConfig> detectDependencies(Path root, String build) {
        return "maven".equals(build)
                ? detectMavenDependencies(root)
                : detectGradleDependencies(root);
    }

    private Map<String, DependencyConfig> detectMavenDependencies(Path root) {
        Map<String, DependencyConfig> deps = new LinkedHashMap<>();

        try {
            String pom = Files.readString(root.resolve("pom.xml"));
            String block = extract(pom, "<dependencies>", "</dependencies>");

            for (String d : block.split("<dependency>")) {
                if (!d.contains("</dependency>")) continue;

                String groupId = extractSafe(d, "<groupId>", "</groupId>");
                String artifactId = extractSafe(d, "<artifactId>", "</artifactId>");
                String version = extractSafe(d, "<version>", "</version>");
                String scope = extractSafe(d, "<scope>", "</scope>");
                String optionalTag = extractSafe(d, "<optional>", "</optional>");

                if (groupId == null || artifactId == null) continue;

                DependencyConfig cfg = new DependencyConfig();
                cfg.version = version;
                cfg.scope = mapScope(scope);
                if ("true".equalsIgnoreCase(optionalTag)) {
                    cfg.optional = true;
                }
                DependencyRuleRegistry.applyRules(groupId, artifactId, cfg);

                String key = groupId + ":" + artifactId;
                deps.put(key, cfg);

                if (!MavenCentralValidator.exists(groupId, artifactId)) {
                    System.out.println(
                        "⚠ Dependency not found on Maven Central: " + key
                    );
                }
            }
        } catch (Exception ignored) {}

        return deps;
    }

    private Map<String, DependencyConfig> detectGradleDependencies(Path root) {
        Map<String, DependencyConfig> deps = new LinkedHashMap<>();

        Path file = Files.exists(root.resolve("build.gradle"))
                ? root.resolve("build.gradle")
                : root.resolve("build.gradle.kts");

        try {
            for (String line : Files.readAllLines(file)) {
                line = line.trim();
                if (!line.contains(":")) continue;

                String scope =
                    line.startsWith("implementation") ? "implementation" :
                    line.startsWith("testImplementation") ? "test" :
                    line.startsWith("runtimeOnly") ? "runtime" :
                    line.startsWith("compileOnly") ? "compileOnly" :
                    null;

                if (scope == null) continue;

                String coords = extractGradleCoords(line);
                if (coords == null) continue;

                String[] p = coords.split(":");
                if (p.length < 2) continue;

                DependencyConfig cfg = new DependencyConfig();
                cfg.version = p.length > 2 ? p[2] : null;
                cfg.scope = scope;
                DependencyRuleRegistry.applyRules(p[0], p[1], cfg);

                String key = p[0] + ":" + p[1];
                deps.put(key, cfg);

                if (!MavenCentralValidator.exists(p[0], p[1])) {
                    System.out.println(
                        "⚠ Dependency not found on Maven Central: " + key
                    );
                }
            }
        } catch (Exception ignored) {}

        return deps;
    }

    // --------------------------------------------------
    // SBX config
    // --------------------------------------------------

    private void writeSbxConfig(
            Path root,
            String name,
            int java,
            String springBoot,
            String build,
            String configFormat,
            Map<String, DependencyConfig> dependencies
    ) {
        SbxConfig cfg = new SbxConfig();
        ProjectConfig project = new ProjectConfig();
        RuntimeConfig runtime = new RuntimeConfig();
        
        String buildToolName = null;
        
        if(build.contains("maven")) {
        	buildToolName = "maven";
        }
        if(build.contains("gradle")){
        	buildToolName = "gradle";
        }

        set(project, "name", name);
        set(project, "type", "monolith");

        set(runtime, "java", java);
        set(runtime, "springBoot", springBoot);
        set(runtime, "build", buildToolName);
        set(runtime, "configFormat", configFormat);

        set(cfg, "schema", 1);
        set(cfg, "project", project);
        set(cfg, "runtime", runtime);
        set(cfg, "dependencies", dependencies);

        SbxConfigWriter.write(root.resolve("sbx.json"), cfg);
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    private String extract(String src, String start, String end) {
        int s = src.indexOf(start);
        int e = src.indexOf(end);
        if (s == -1 || e == -1) throw new IllegalStateException();
        return src.substring(s + start.length(), e).trim();
    }

    private String extractSafe(String src, String start, String end) {
        int s = src.indexOf(start);
        int e = src.indexOf(end);
        if (s == -1 || e == -1) return null;
        return src.substring(s + start.length(), e).trim();
    }

    private String extractGradleCoords(String line) {
        int q1 = line.indexOf("\"");
        int q2 = line.lastIndexOf("\"");
        if (q1 == -1 || q2 <= q1) return null;
        return line.substring(q1 + 1, q2);
    }

    private String mapScope(String scope) {
        if (scope == null) return "implementation";
        return switch (scope) {
            case "test" -> "test";
            case "runtime" -> "runtime";
            case "provided" -> "compileOnly";
            default -> "implementation";
        };
    }

    private boolean confirm(String q) {
        System.out.print(q + " ");
        return scanner.nextLine().trim().equalsIgnoreCase("yes");
    }

    private String ask(String q) {
        System.out.print(q + ": ");
        String v = scanner.nextLine().trim();
        if (v.isEmpty()) throw new IllegalStateException(q + " is required");
        return v;
    }

    private void set(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("SBX internal error: " + field, e);
        }
    }
    private String normalizeReleaseBootVersion(String bootVersion) {
        if (bootVersion == null) {
            return null;
        }

        if (bootVersion.endsWith(".RELEASE")) {
            return bootVersion.substring(0, bootVersion.length() - ".RELEASE".length());
        }

        return bootVersion;
    }
}