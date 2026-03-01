package io.github.mdasifmustafa.sbx.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;

class DoctorCommandTest {

    private final DoctorCommand doctorCommand = new DoctorCommand();

    @Test
    void dependenciesPassWhenOnlyBomManagedVersionsExist() {
        Map<String, DependencyConfig> deps = new LinkedHashMap<>();
        deps.put("org.springframework.boot:spring-boot-starter-web", dependency(null));
        deps.put("org.springframework.boot:spring-boot-starter-validation", dependency(null));

        DoctorCommand.CheckResult result = doctorCommand.analyzeDependencies(deps);

        assertEquals(DoctorCommand.Status.PASS, result.status);
        assertEquals("Dependencies", result.name);
        assertEquals("Found 2 dependencies", result.message);
        assertTrue(result.details.contains("managed externally (BOM)"));
    }

    @Test
    void dependenciesWarnWhenMvcAndWebFluxAreBothPresent() {
        Map<String, DependencyConfig> deps = new LinkedHashMap<>();
        deps.put("org.springframework.boot:spring-boot-starter-web", dependency(null));
        deps.put("org.springframework.boot:spring-boot-starter-webflux", dependency("1.0.0"));

        DoctorCommand.CheckResult result = doctorCommand.analyzeDependencies(deps);

        assertEquals(DoctorCommand.Status.WARN, result.status);
        assertEquals("Found 2 dependencies", result.message);
        assertTrue(result.details.contains("MVC + WebFlux"));
    }

    @Test
    void javaCompatibilityClassifiesFailWarnAndPass() {
        DoctorCommand.CheckResult fail = doctorCommand.analyzeJavaVersion(17, 21);
        assertEquals(DoctorCommand.Status.FAIL, fail.status);
        assertTrue(fail.details.contains("System: Java 17 | Project: Java 21"));

        DoctorCommand.CheckResult warn = doctorCommand.analyzeJavaVersion(25, 21);
        assertEquals(DoctorCommand.Status.WARN, warn.status);
        assertTrue(warn.details.contains("System: Java 25 | Project: Java 21"));

        DoctorCommand.CheckResult pass = doctorCommand.analyzeJavaVersion(21, 21);
        assertEquals(DoctorCommand.Status.PASS, pass.status);
        assertTrue(pass.details.contains("System: Java 21 | Project: Java 21"));
    }

    private static DependencyConfig dependency(String version) {
        DependencyConfig dependency = new DependencyConfig();
        dependency.version = version;
        return dependency;
    }
}
