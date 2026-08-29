package io.github.mdasifmustafa.sbx.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.ux.SbxResponse;

public final class DependencyManager {

    private static final Path POM = Path.of("pom.xml");

    private record Dep(String groupId, String artifactId, String version, String extra) {}

    private static final Map<String, Dep> KNOWN = new LinkedHashMap<>();

    static {
        KNOWN.put("lombok", new Dep("org.projectlombok", "lombok", "1.18.26", "        <scope>provided</scope>"));
        KNOWN.put("validation", new Dep("org.springframework.boot", "spring-boot-starter-validation", "", ""));
        // Web starter used by controllers / REST endpoints
        KNOWN.put("web", new Dep("org.springframework.boot", "spring-boot-starter-web", "", ""));
        KNOWN.put("mapstruct", new Dep("org.mapstruct", "mapstruct", "1.5.5.Final", ""));
        KNOWN.put("mapstruct-processor", new Dep("org.mapstruct", "mapstruct-processor", "1.5.5.Final", "        <scope>provided</scope>"));
        KNOWN.put("spring-data-jpa", new Dep("org.springframework.boot", "spring-boot-starter-data-jpa", "", ""));
        KNOWN.put("mysql-connector", new Dep("mysql", "mysql-connector-java", "8.1.0", ""));
        KNOWN.put("graphql", new Dep("org.springframework.boot", "spring-boot-starter-graphql", "", ""));
        KNOWN.put("mail", new Dep("org.springframework.boot", "spring-boot-starter-mail", "", ""));
        KNOWN.put("flyway", new Dep("org.flywaydb", "flyway-core", "", ""));
        KNOWN.put("cache", new Dep("org.springframework.boot", "spring-boot-starter-cache", "", ""));
        KNOWN.put("caffeine", new Dep("com.github.ben-manes.caffeine", "caffeine", "3.1.8", ""));
        KNOWN.put("redis", new Dep("org.springframework.boot", "spring-boot-starter-data-redis", "", ""));
        KNOWN.put("security", new Dep("org.springframework.boot", "spring-boot-starter-security", "", ""));
        KNOWN.put("test", new Dep("org.springframework.boot", "spring-boot-starter-test", "", ""));
    }

    private DependencyManager() {}

    public static void ensureDependencies() throws IOException {
        // Backwards-compatible: check all known dependencies
        ensureDependencies(KNOWN.keySet().toArray(new String[0]));
    }

    public static void ensureDependencies(String... keys) throws IOException {
        ensureDependencies(false, keys);
    }

    /**
     * Ensure only the named dependency keys are present in pom.xml.
     * If autoApply is true, missing dependencies are added without prompting.
     */
    public static void ensureDependencies(boolean autoApply, String... keys) throws IOException {
        if (keys == null || keys.length == 0) {
            // nothing required for this command
            return;
        }

        if (!Files.exists(POM)) {
            SbxResponse.info("pom.xml not found. Skipping dependency checks.");
            return;
        }

        String pom = Files.readString(POM);

        // If MapStruct is requested, proactively ensure Lombok is present (helps annotation processing).
        boolean mapstructRequested = false;
        if (keys != null) {
            for (String k : keys) {
                if ("mapstruct".equals(k) || "mapstruct-processor".equals(k)) {
                    mapstructRequested = true;
                    break;
                }
            }
        }

        if (mapstructRequested) {
            Dep lombokDep = KNOWN.get("lombok");
            if (lombokDep != null && !containsDependency(pom, lombokDep.groupId, lombokDep.artifactId)) {
                boolean addLombok = autoApply || promptAdd(lombokDep.groupId, lombokDep.artifactId);
                if (addLombok) {
                    pom = addDependencyToPom(pom, lombokDep);
                    Files.writeString(POM, pom);
                    SbxResponse.success("Added dependency: " + lombokDep.groupId + ":" + lombokDep.artifactId);
                } else {
                    SbxResponse.tip("Skipped adding lombok. MapStruct may require lombok to build generated code.");
                }
            }
        }

        for (String key : keys) {
            Dep dep = KNOWN.get(key);
            if (dep == null) {
                // unknown key, skip
                continue;
            }
            if (!containsDependency(pom, dep.groupId, dep.artifactId)) {
                boolean add = autoApply || promptAdd(dep.groupId, dep.artifactId);
                if (add) {
                    pom = addDependencyToPom(pom, dep);
                    // If MapStruct is being added, ensure compiler plugin annotationProcessorPaths
                    if ("mapstruct".equals(dep.artifactId) || "mapstruct-processor".equals(dep.artifactId)) {
                        pom = ensureMapStructCompilerPlugin(pom);
                        // Ensure lombok is present; it may have been added above, but double-check
                        Dep lombokDep = KNOWN.get("lombok");
                        if (lombokDep != null && !containsDependency(pom, lombokDep.groupId, lombokDep.artifactId)) {
                            pom = addDependencyToPom(pom, lombokDep);
                        }
                    }
                    Files.writeString(POM, pom);
                    SbxResponse.success("Added dependency: " + dep.groupId + ":" + dep.artifactId);
                } else {
                    SbxResponse.tip("Skipped adding " + dep.artifactId + ". You can add it later manually.");
                }
            }
        }
    }

    private static boolean containsDependency(String pom, String groupId, String artifactId) {
        return pom.contains("<groupId>" + groupId + "</groupId>") && pom.contains("<artifactId>" + artifactId + "</artifactId>");
    }

    private static boolean promptAdd(String groupId, String artifactId) {
        String prompt = "Add dependency " + groupId + ":" + artifactId + " to pom.xml? (Y/n): ";
        String resp = null;
        try {
            if (System.console() != null) {
                resp = System.console().readLine(prompt);
            } else {
                System.out.print(prompt);
                Scanner sc = new Scanner(System.in);
                resp = sc.nextLine();
            }
        } catch (Exception ex) {
            return false;
        }

        if (resp == null || resp.isBlank()) {
            return true;
        }
        resp = resp.trim().toLowerCase();
        return resp.equals("y") || resp.equals("yes");
    }

    private static String addDependencyToPom(String pom, Dep dep) {
        String dependencyXml = buildDependencyXml(dep);

        if (pom.contains("<dependencies>")) {
            return pom.replaceFirst("(?s)(</dependencies>)", dependencyXml + "$1");
        }

        // add dependencies block before </project>
        if (pom.contains("</project>")) {
            return pom.replaceFirst("</project>", "    <dependencies>\n" + dependencyXml + "    </dependencies>\n</project>");
        }

        // fallback - append
        return pom + "\n<dependencies>\n" + dependencyXml + "</dependencies>\n";
    }

    /**
     * Ensure maven-compiler-plugin is configured with MapStruct (and Lombok) annotation processors.
     * This helps projects that require explicit annotationProcessorPaths (module-aware builds).
     */
    private static String ensureMapStructCompilerPlugin(String pom) {
        // If plugin already configured to reference mapstruct-processor, skip
        if (pom.contains("mapstruct-processor")) {
            return pom;
        }

        String pluginXml = "        <plugin>\n"
                + "            <groupId>org.apache.maven.plugins</groupId>\n"
                + "            <artifactId>maven-compiler-plugin</artifactId>\n"
                + "            <version>3.11.0</version>\n"
                + "            <configuration>\n"
                + "                <annotationProcessorPaths>\n"
                + "                    <path>\n"
                + "                        <groupId>org.mapstruct</groupId>\n"
                + "                        <artifactId>mapstruct-processor</artifactId>\n"
                + "                        <version>1.5.5.Final</version>\n"
                + "                    </path>\n"
                + "                    <path>\n"
                + "                        <groupId>org.projectlombok</groupId>\n"
                + "                        <artifactId>lombok</artifactId>\n"
                + "                        <version>1.18.26</version>\n"
                + "                    </path>\n"
                + "                </annotationProcessorPaths>\n"
                + "            </configuration>\n"
                + "        </plugin>\n";

        // If a <plugins> block exists, insert the plugin before </plugins>
        if (pom.contains("<plugins>")) {
            return pom.replaceFirst("(?s)(</plugins>)", pluginXml + "$1");
        }

        // If a <build> exists but no <plugins>, add plugins block
        if (pom.contains("<build>")) {
            return pom.replaceFirst("(?s)(</build>)", "    <plugins>\n" + pluginXml + "    </plugins>\n$1");
        }

        // Otherwise add a full build/plugins block before </project>
        if (pom.contains("</project>")) {
            String buildBlock = "    <build>\n" + "    <plugins>\n" + pluginXml + "    </plugins>\n" + "    </build>\n";
            return pom.replaceFirst("</project>", buildBlock + "</project>");
        }

        // Fallback: append
        return pom + "\n<build>\n<plugins>\n" + pluginXml + "</plugins>\n</build>\n";
    }

    private static String buildDependencyXml(Dep dep) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <dependency>\n");
        sb.append("            <groupId>").append(dep.groupId).append("</groupId>\n");
        sb.append("            <artifactId>").append(dep.artifactId).append("</artifactId>\n");
        if (dep.version != null && !dep.version.isBlank()) {
            sb.append("            <version>").append(dep.version).append("</version>\n");
        }
        if (dep.extra != null && !dep.extra.isBlank()) {
            sb.append(dep.extra).append("\n");
        }
        sb.append("        </dependency>\n");
        return sb.toString();
    }
}
