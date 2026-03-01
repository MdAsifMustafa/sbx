package io.github.mdasifmustafa.sbx.command;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.mdasifmustafa.sbx.config.ProjectConfig;
import io.github.mdasifmustafa.sbx.config.RuntimeConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.initializr.InitializrOption;
import io.github.mdasifmustafa.sbx.initializr.InitializrSingleSelect;
import io.github.mdasifmustafa.sbx.initializr.SpringInitializrMetadata;
import io.github.mdasifmustafa.sbx.initializr.SpringInitializrMetadataClient;
import io.github.mdasifmustafa.sbx.io.SbxConfigWriter;
import io.github.mdasifmustafa.sbx.runtime.SystemJavaDetector;
import io.github.mdasifmustafa.sbx.template.SpringBootProjectGenerator;
import io.github.mdasifmustafa.sbx.ux.SbxOptionLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "new",
        description = "Create a new SBX application",
        mixinStandardHelpOptions = true
)
public class AppNewCommand implements Runnable {

    // --------------------------------------------------
    // Positional
    // --------------------------------------------------
    @Parameters(index = "0", description = "Project name")
    private String name;

    // --------------------------------------------------
    // Optional flags (non-interactive)
    // --------------------------------------------------
    @Option(names = "--group-id")
    private String groupId;

    @Option(names = "--artifact-id")
    private String artifactId;

    @Option(names = "--package-name")
    private String packageName;

    @Option(names = "--description")
    private String description;

    @Option(names = "--java")
    private String javaVersion;

    @Option(names = "--boot")
    private String bootVersion;

    @Option(names = "--build")
    private String buildTool;

    @Option(names = "--packaging")
    private String packaging;

    @Option(names = "--language")
    private String language;
    
	@Option(names = "--config", description = "Configuration format: properties or yaml")
    private String configFormat;
	@Option(names = "--allow-legacy", description = "Allow legacy Spring Boot versions")
	boolean allowLegacy;

    // --------------------------------------------------

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("🚀 Creating new SBX application\n");

        SpringInitializrMetadata metadata =
                SpringInitializrMetadataClient.fetch();

        // --------------------------------------------------
        // Free-text fields (prompt if missing)
        // --------------------------------------------------
        groupId = valueOrAsk(groupId, "Group ID (e.g. org.example)");
        artifactId = valueOrAsk(artifactId, "Artifact ID");
        String suggestedPackage = defaultPackage(groupId, artifactId);

        packageName = valueOrAskWithDefault(
                packageName,
                "Base package",
                suggestedPackage
        );
        description = valueOrDefault(description, "SBX application");

        // --------------------------------------------------
        // Selectable fields (flags OR interactive)
        // --------------------------------------------------
        packaging = selectIfMissing(
                packaging,
                "Packaging",
                metadata.packaging()
        );

        language = selectIfMissing(
                language,
                "Language",
                metadata.language()
        );

        buildTool = selectIfMissing(
                buildTool,
                "Build tool",
                metadata.type().onlyStarterZip()
        );

        List<String> bootVersions = mergedBootVersions(metadata);

        System.out.println("Spring Boot version:");
        for (int i = 0; i < bootVersions.size(); i++) {
            String v = bootVersions.get(i);

            boolean legacy =
                metadata.bootVersion().values()
                    .stream()
                    .noneMatch(o -> o.id().equals(v));

            System.out.printf(
                "  %d) %s%s%n",
                i + 1,
                v,
                legacy ? "  [legacy]" : ""
            );
        }

        System.out.print("Select [1]: ");
        String input = scanner.nextLine().trim();
        bootVersion = input.isEmpty()
                ? bootVersions.get(0)
                : bootVersions.get(Integer.parseInt(input) - 1);
        
        if (isLegacyBoot(bootVersion)) {
            handleLegacyConfirmation(bootVersion);
        }
        
        configFormat = selectIfMissingSimple(
                configFormat,
                "Configuration format",
                SbxOptionLoader.configFormats(),
                "properties"
        );

        List<Integer> javaOptions =
                resolveJavaOptions(bootVersion, metadata);

        int detected = SystemJavaDetector.detectMajor();

        // recommended = detected if supported, else first option
        int recommended = javaOptions.contains(detected)
                ? detected
                : javaOptions.get(0);

        System.out.println("Java version:");
        for (int i = 0; i < javaOptions.size(); i++) {
            int v = javaOptions.get(i);
            String mark = (v == recommended)
                    ? "  ← recommended" +
                      (v == detected ? " (detected Java " + detected + ")" : "")
                    : "";
            System.out.printf("  %d) %d%s%n", i + 1, v, mark);
        }

        System.out.print("Select [" + (javaOptions.indexOf(recommended) + 1) + "]: ");
        String inputJava = scanner.nextLine().trim();

        javaVersion = inputJava.isEmpty()
                ? String.valueOf(recommended)
                : String.valueOf(javaOptions.get(Integer.parseInt(inputJava) - 1));
        

        Path projectRoot = Path.of(name);
        if (Files.exists(projectRoot)) {
            throw new IllegalStateException("Directory already exists: " + name);
        }

        try {
            Files.createDirectories(projectRoot);

            boolean legacy = isLegacyBoot(bootVersion);

            if (legacy) {
                SpringBootProjectGenerator.generate(
                    projectRoot,
                    groupId,
                    artifactId,
                    packageName,
                    bootVersion,
                    Integer.parseInt(javaVersion),
                    buildTool.toLowerCase().contains("gradle") ? "gradle" : "maven",
                    packaging.toLowerCase(),
                    language.toLowerCase(),
                    configFormat.toLowerCase()
                );
            } else {
                generateFromInitializr(projectRoot);
            }

            createSbxRuntime(projectRoot);
            writeSbxConfig(projectRoot);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SBX application", e);
        }

        System.out.println("\n✔ SBX application created: " + name);
    }

    // ==================================================
    // Initializr generation
    // ==================================================

    private void generateFromInitializr(Path root) throws Exception {
    	
        String url = "https://start.spring.io/starter.zip?" +
                param("type", buildTool) +
                param("language", language) +
                param("packaging", packaging) +
                param("bootVersion", normalizeReleaseBootVersion(bootVersion)) +
                param("javaVersion", javaVersion) +
                param("groupId", groupId) +
                param("artifactId", artifactId) +
                param("name", name) +
                param("description", description) +
                param("configurationFileFormat", configFormat) +
                param("packageName", packageName);
        
        System.out.println(url);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<InputStream> res =
                client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        unzip(res.body(), root);
        applyConfigFormat(root, configFormat);
        
    }
    private boolean isLegacyBoot(String bootVersion) {
        return SbxOptionLoader
                .legacyBootVersions()
                .contains(bootVersion);
    }
    private void handleLegacyConfirmation(String bootVersion) {
        // Non-interactive (CI / script)
        if (!isInteractive() && !allowLegacy) {
            throw new IllegalStateException(
                "ERROR: Spring Boot " + bootVersion + " is legacy.\n" +
                "Use --allow-legacy to proceed."
            );
        }

        // Interactive confirmation
        if (isInteractive() && !allowLegacy) {
            confirmLegacy(bootVersion);
        }
    }
    private boolean isInteractive() {
        return System.console() != null;
    }
    private void confirmLegacy(String bootVersion) {
        System.out.println();
        System.out.println("⚠ Legacy Spring Boot version selected: " + bootVersion);
        System.out.println("• End of OSS support: Nov 2023");
        System.out.println("• Still available on Maven Central");
        System.out.println("• No security guarantees");
        System.out.println();

        System.out.print("Continue? (Y/n): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("n")) {
            throw new IllegalStateException("Aborted by user");
        }
    }
    private String valueOrAskWithDefault(String value, String question, String def) {
        if (value != null) return value;

        System.out.print(question + " [" + def + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? def : input;
    }
    private String defaultPackage(String groupId, String artifactId) {
        return (groupId + "." + artifactId)
                .replaceAll("[^a-zA-Z0-9_.]", "")
                .toLowerCase();
    }

    private void unzip(InputStream zip, Path targetDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(zip)) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                Path path = targetDir.resolve(e.getName());
                if (e.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    Files.createDirectories(path.getParent());
                    Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private String param(String k, String v) {
        return k + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8) + "&";
    }

    // ==================================================
    // Selection helpers
    // ==================================================

    private String selectIfMissing(
            String current,
            String label,
            InitializrSingleSelect select
    ) {
        if (current != null) {
            return current;
        }

        System.out.println(label + ":");
        List<InitializrOption> opts = select.values();

        int def = -1;
        for (int i = 0; i < opts.size(); i++) {
            if (opts.get(i).isDefault()) def = i;
            System.out.printf("  %d) %s%n", i + 1, opts.get(i).name());
        }

        System.out.print("Select [" + (def + 1) + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty()
                ? opts.get(def).id()
                : opts.get(Integer.parseInt(input) - 1).id();
    }

    // ==================================================
    // SBX metadata
    // ==================================================

    private void createSbxRuntime(Path root) throws Exception {
        Files.createDirectories(root.resolve("sbx/runtime"));
    }

    private void writeSbxConfig(Path root) {
        SbxConfig cfg = new SbxConfig();
        ProjectConfig project = new ProjectConfig();
        RuntimeConfig runtime = new RuntimeConfig();

        set(project, "name", name);
        set(project, "type", "monolith");
        
        String buildToolName = null;
        
        if(buildTool.contains("maven")) {
        	buildToolName = "maven";
        }
        if(buildTool.contains("gradle")){
        	buildToolName = "gradle";
        }

        set(runtime, "java", Integer.parseInt(javaVersion));
        set(runtime, "springBoot", normalizeReleaseBootVersion(bootVersion));
        set(runtime, "build", buildToolName);
        set(runtime, "configFormat", configFormat);

        set(cfg, "schema", 1);
        set(cfg, "project", project);
        set(cfg, "runtime", runtime);

        SbxConfigWriter.write(root.resolve("sbx.json"), cfg);
    }

    private void set(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ==================================================
    // Simple input helpers
    // ==================================================

    private String valueOrAsk(String v, String q) {
        if (v != null) return v;
        System.out.print(q + ": ");
        return scanner.nextLine().trim();
    }

    private String valueOrDefault(String v, String d) {
        return v != null ? v : d;
    }
    
    private String selectIfMissingSimple(
            String current,
            String label,
            List<String> options,
            String def
    ) {
        if (current != null) return current;

        System.out.println(label + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, options.get(i));
        }

        int defIdx = options.indexOf(def);
        System.out.print("Select [" + (defIdx + 1) + "]: ");

        String input = scanner.nextLine().trim();
        return input.isEmpty()
                ? def
                : options.get(Integer.parseInt(input) - 1);
    }
    private void applyConfigFormat(Path root, String format) throws Exception {
        Path resources = root.resolve("src/main/resources");
        Path props = resources.resolve("application.properties");
        Path yaml = resources.resolve("application.yml");

        if ("yaml".equals(format)) {
            if (Files.exists(props)) {
                Files.move(props, yaml, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if (Files.exists(yaml)) {
                Files.move(yaml, props, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    private List<String> mergedBootVersions(SpringInitializrMetadata meta) {
        Set<String> versions = new LinkedHashSet<>();

        // official (from Spring Initializr)
        meta.bootVersion().values()
            .forEach(v -> versions.add(v.id()));

        // legacy (from options.json)
        versions.addAll(SbxOptionLoader.legacyBootVersions());

        return new ArrayList<>(versions);
    }
    private List<Integer> resolveJavaOptions(
            String bootVersion,
            SpringInitializrMetadata metadata
    ) {
        // If legacy boot → use SBX options.json
    	String normalized = normalizeBootVersion(bootVersion);
        List<Integer> legacy = SbxOptionLoader.javaCompatibility(normalized);
        if (!legacy.isEmpty()) {
            return legacy;
        }

        // Else → use Spring Initializr metadata
        return metadata.javaVersion().values().stream()
                .map(j -> Integer.parseInt(j.id()))
                .toList();
    }
    
    private String normalizeBootVersion(String bootVersion) {
        // Examples:
        // 2.7.18.RELEASE → 2.7.18
        // 3.5.10.BUILD-SNAPSHOT → 3.5.10
        // 4.1.0.M1 → 4.1.0
        int count = 0;

        for (int i = 0; i < bootVersion.length(); i++) {
            if (bootVersion.charAt(i) == '.') count++;
            if (count == 2) {
                int end = i;
                while (end + 1 < bootVersion.length()
                        && Character.isDigit(bootVersion.charAt(end + 1))) {
                    end++;
                }
                return bootVersion.substring(0, end + 1);
            }
        }
        return bootVersion;
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