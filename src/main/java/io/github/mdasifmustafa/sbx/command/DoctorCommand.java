package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.io.SbxConfigValidator;
import io.github.mdasifmustafa.sbx.runtime.AppInfo;
import io.github.mdasifmustafa.sbx.runtime.BuildToolDetector;
import io.github.mdasifmustafa.sbx.runtime.SystemJavaDetector;
import io.github.mdasifmustafa.sbx.ux.Console;
import picocli.CommandLine.Command;

@Command(name = "doctor", description = "Validate environment and SBX configuration")
public class DoctorCommand implements Runnable {

    private static final String HEADER_SEPARATOR = "=============================================";
    private static final String SECTION_SEPARATOR = "---------------------------------------------";

    static final class CheckResult {
        final String name;
        final Status status;
        final String message;
        final String details;

        CheckResult(String name, Status status, String message, String details) {
            this.name = name;
            this.status = status;
            this.message = message;
            this.details = details;
        }

        void print() {
            StringBuilder out = new StringBuilder(name);
            if (message != null && !message.isEmpty()) {
                out.append(" - ").append(message);
            }
            if (details != null && !details.isEmpty()) {
                out.append("\n      ").append(details);
            }
            switch (status) {
                case PASS -> Console.success(out.toString());
                case WARN -> Console.warning(out.toString());
                case FAIL -> Console.error(out.toString());
            }
        }
    }

    enum Status {
        PASS,
        WARN,
        FAIL
    }

    static final class DoctorReport {
        final List<CheckResult> checks = new ArrayList<>();
        int passCount;
        int warnCount;
        int failCount;

        void add(CheckResult check) {
            checks.add(check);
            switch (check.status) {
                case PASS -> passCount++;
                case WARN -> warnCount++;
                case FAIL -> failCount++;
            }
        }
    }

    @Override
    public void run() {
        AppInfo.printBanner();
        System.out.println("Doctor Report");
        System.out.println(HEADER_SEPARATOR + "\n");

        DoctorReport report = evaluate(Path.of("sbx.json"), SystemJavaDetector.detectMajor());
        printResults(report);

        if (report.failCount > 0) {
            System.exit(1);
        }
    }

    DoctorReport evaluate(Path sbxFile, int systemJava) {
        DoctorReport report = new DoctorReport();

        if (!Files.exists(sbxFile)) {
            report.add(new CheckResult(
                    "SBX Configuration",
                    Status.FAIL,
                    "sbx.json not found",
                    "Run 'sbx app new <name>' for new projects or 'sbx init' for existing projects"));
            return report;
        }

        var config = SbxConfigReader.read(sbxFile);

        try {
            SbxConfigValidator.validate(config);
            report.add(new CheckResult("SBX Configuration", Status.PASS, "sbx.json is valid", null));
        } catch (Exception e) {
            report.add(new CheckResult("SBX Configuration", Status.FAIL, "Invalid sbx.json: " + e.getMessage(), null));
        }

        try {
            String buildTool = BuildToolDetector.detect(Path.of("."));
            String toolVersion = buildTool.equalsIgnoreCase("Maven") ? "Maven 3.8+" : "Gradle 7.0+";
            report.add(new CheckResult("Build Tool", Status.PASS, buildTool + " detected", toolVersion));
        } catch (IllegalStateException e) {
            report.add(new CheckResult(
                    "Build Tool",
                    Status.FAIL,
                    "Unable to detect Maven or Gradle",
                    "Ensure pom.xml or build.gradle exists in project root"));
        }

        int projectJava = config.getRuntime().getJava();
        report.add(analyzeJavaVersion(systemJava, projectJava));

        String boot = config.getRuntime().getSpringBoot();
        if (boot.startsWith("2.")) {
            report.add(new CheckResult(
                    "Spring Boot",
                    Status.WARN,
                    "Legacy Spring Boot: " + boot,
                    "Consider upgrading to Spring Boot 3.x for security patches"));
        } else {
            report.add(new CheckResult("Spring Boot", Status.PASS, "Spring Boot " + boot, null));
        }

        if (boot.startsWith("3.") && projectJava < 17) {
            report.add(new CheckResult("Compatibility (Boot -> Java)", Status.FAIL, "Spring Boot 3.x requires Java 17+", null));
        } else if (boot.startsWith("3.")) {
            report.add(new CheckResult(
                    "Compatibility (Boot -> Java)",
                    Status.PASS,
                    "Spring Boot 3.x with Java " + projectJava,
                    null));
        }

        report.add(analyzeDependencies(config.getDependencies()));
        return report;
    }

    CheckResult analyzeJavaVersion(int systemJava, int projectJava) {
        String javaStatus = String.format("System: Java %d | Project: Java %d", systemJava, projectJava);
        if (systemJava < projectJava) {
            return new CheckResult(
                    "Java Version",
                    Status.FAIL,
                    "System Java is lower than project requirement",
                    javaStatus);
        }
        if (systemJava == projectJava) {
            return new CheckResult("Java Version", Status.PASS, "Java version compatible", javaStatus);
        }
        return new CheckResult("Java Version", Status.WARN, "Version mismatch detected", javaStatus);
    }

    CheckResult analyzeDependencies(Map<String, DependencyConfig> deps) {
        if (deps == null || deps.isEmpty()) {
            return new CheckResult(
                    "Dependencies",
                    Status.WARN,
                    "No dependencies detected in sbx.json",
                    "Run 'sbx init' to scan and update dependencies");
        }

        List<String> issues = new ArrayList<>();
        boolean hasWebMvc = false;
        boolean hasWebFlux = false;
        int externalVersions = 0;

        for (var entry : deps.entrySet()) {
            String ga = entry.getKey();
            DependencyConfig d = entry.getValue();

            if (d.version == null) {
                externalVersions++;
            }
            if (ga.endsWith("spring-boot-starter-web")) {
                hasWebMvc = true;
            }
            if (ga.endsWith("spring-boot-starter-webflux")) {
                hasWebFlux = true;
            }
        }

        if (hasWebMvc && hasWebFlux) {
            issues.add("Conflicting web stacks detected (MVC + WebFlux)");
        }

        if (!issues.isEmpty()) {
            String details = "Notes: " + String.join(", ", issues);
            if (externalVersions > 0) {
                details += "; " + externalVersions + " dependency versions managed externally (BOM)";
            }
            return new CheckResult("Dependencies", Status.WARN, "Found " + deps.size() + " dependencies", details);
        }

        String details = null;
        if (externalVersions > 0) {
            details = externalVersions + " dependency versions managed externally (BOM)";
        }
        return new CheckResult("Dependencies", Status.PASS, "Found " + deps.size() + " dependencies", details);
    }

    private void printResults(DoctorReport report) {
        System.out.println("Checks:");
        System.out.println(SECTION_SEPARATOR);
        for (CheckResult check : report.checks) {
            check.print();
        }

        System.out.println();
        System.out.println("Summary:");
        System.out.println(SECTION_SEPARATOR);

        Console.success(report.passCount + " passed");
        if (report.warnCount > 0) {
            Console.warning(report.warnCount + " warnings");
        }
        if (report.failCount > 0) {
            Console.error(report.failCount + " failed");
            System.out.println();
            System.out.println("  Environment is NOT healthy. Please fix the issues above.");
        } else {
            System.out.println();
            Console.success("Environment looks healthy!");
        }
        System.out.println();
    }
}
