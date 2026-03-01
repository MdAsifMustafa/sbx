package io.github.mdasifmustafa.sbx.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpringBootProjectGenerator {

    public static void generate(
            Path root,
            String groupId,
            String artifactId,
            String packageName,
            String bootVersion,
            int javaVersion,
            String buildTool,      // maven | gradle
            String packaging,      // jar | war
            String language,       // java | kotlin | groovy
            String configFormat    // properties | yaml
    ) throws IOException {

        boolean maven = buildTool.startsWith("maven");
        boolean war = packaging.equalsIgnoreCase("war");

        // ---------------- Build files ----------------
        if (maven) {
            write(root.resolve("pom.xml"),
                pomXml(groupId, artifactId, bootVersion, javaVersion, packaging, language)
            );
        } else {
            write(root.resolve("build.gradle"),
                gradleBuild(artifactId, bootVersion, javaVersion, language)
            );
            write(root.resolve("settings.gradle"),
                "rootProject.name = \"" + artifactId + "\"\n"
            );
        }

        // ---------------- Source paths ----------------
        String pkgPath = packageName.replace('.', '/');
        String ext = languageExt(language);
        String srcRoot = language.equals("kotlin")
                ? "src/main/kotlin/"
                : language.equals("groovy")
                    ? "src/main/groovy/"
                    : "src/main/java/";

        write(
            root.resolve(srcRoot + pkgPath + "/Application." + ext),
            mainClass(language, packageName, war)
        );

        // ---------------- Config ----------------
     // ---------------- Config ----------------
        if (configFormat.equals("yaml")) {
            write(
                root.resolve("src/main/resources/application.yml"),
                configContent("yaml", artifactId)
            );
        } else {
            write(
                root.resolve("src/main/resources/application.properties"),
                configContent("properties", artifactId)
            );
        }

        // ---------------- Test ----------------
        write(
            root.resolve("src/test/" + (language.equals("java") ? "java/" : "kotlin/")
                + pkgPath + "/ApplicationTests." + ext),
            testClass(language, packageName)
        );

        write(root.resolve(".gitignore"),
            "/build/\n/target/\n.idea\n.vscode\n"
        );
    }

    // ==================================================
    // Maven
    // ==================================================

    private static String pomXml(
            String groupId,
            String artifactId,
            String bootVersion,
            int javaVersion,
            String packaging,
            String language
    ) {
        String langDeps = switch (language) {
            case "kotlin" -> """
                <dependency>
                  <groupId>org.jetbrains.kotlin</groupId>
                  <artifactId>kotlin-reflect</artifactId>
                </dependency>
                """;
            case "groovy" -> """
                <dependency>
                  <groupId>org.codehaus.groovy</groupId>
                  <artifactId>groovy</artifactId>
                </dependency>
                """;
            default -> "";
        };

        return """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>

              <parent>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>%s</version>
              </parent>

              <groupId>%s</groupId>
              <artifactId>%s</artifactId>
              <packaging>%s</packaging>

              <properties>
                <java.version>%d</java.version>
              </properties>

              <dependencies>
                <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
                %s
              </dependencies>
            </project>
            """.formatted(
                bootVersion,
                groupId,
                artifactId,
                packaging,
                javaVersion,
                langDeps
            );
    }

    // ==================================================
    // Gradle
    // ==================================================

    private static String gradleBuild(
            String artifactId,
            String bootVersion,
            int javaVersion,
            String language
    ) {
        String langPlugin = switch (language) {
            case "kotlin" -> "id 'org.jetbrains.kotlin.jvm' version '1.9.0'";
            case "groovy" -> "id 'groovy'";
            default -> "id 'java'";
        };

        return """
            plugins {
                %s
                id 'org.springframework.boot' version '%s'
                id 'io.spring.dependency-management'
            }

            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(%d)
                }
            }

            repositories { mavenCentral() }

            dependencies {
                implementation 'org.springframework.boot:spring-boot-starter-web'
                testImplementation 'org.springframework.boot:spring-boot-starter-test'
            }
            """.formatted(
                langPlugin,
                bootVersion,
                javaVersion
            );
    }

    // ==================================================
    // Source generators
    // ==================================================

    private static String mainClass(String lang, String pkg, boolean war) {
        return switch (lang) {
            case "kotlin" -> """
                package %s

                import org.springframework.boot.autoconfigure.SpringBootApplication
                import org.springframework.boot.runApplication

                @SpringBootApplication
                class Application

                fun main(args: Array<String>) {
                    runApplication<Application>(*args)
                }
                """.formatted(pkg);

            case "groovy" -> """
                package %s

                import org.springframework.boot.autoconfigure.SpringBootApplication
                import org.springframework.boot.SpringApplication

                @SpringBootApplication
                class Application {
                    static void main(String[] args) {
                        SpringApplication.run(Application, args)
                    }
                }
                """.formatted(pkg);

            default -> """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class Application {
                    public static void main(String[] args) {
                        SpringApplication.run(Application.class, args);
                    }
                }
                """.formatted(pkg);
        };
    }

    private static String testClass(String lang, String pkg) {
        return lang.equals("kotlin")
            ? """
                package %s

                import org.junit.jupiter.api.Test
                import org.springframework.boot.test.context.SpringBootTest

                @SpringBootTest
                class ApplicationTests {
                    @Test fun contextLoads() {}
                }
                """.formatted(pkg)
            : """
                package %s;

                import org.junit.jupiter.api.Test;
                import org.springframework.boot.test.context.SpringBootTest;

                @SpringBootTest
                class ApplicationTests {
                    @Test void contextLoads() {}
                }
                """.formatted(pkg);
    }

    private static String configContent(String format, String name) {
        return format.equals("yaml")
            ? "spring:\n  application:\n    name: " + name + "\n"
            : "spring.application.name=" + name + "\n";
    }

    private static String languageExt(String lang) {
        return lang.equals("kotlin") ? "kt" : lang.equals("groovy") ? "groovy" : "java";
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}