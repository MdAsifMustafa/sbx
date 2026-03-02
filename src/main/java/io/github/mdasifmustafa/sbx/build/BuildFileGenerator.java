package io.github.mdasifmustafa.sbx.build;

import io.github.mdasifmustafa.sbx.config.SbxConfig;

import java.nio.file.Files;
import java.nio.file.Path;

public class BuildFileGenerator {

    public static void generate(Path root,
                                SbxConfig config,
                                String target,
                                String dsl) {

        if ("maven".equalsIgnoreCase(target)) {
            generateMaven(root, config);
            return;
        }

        generateGradle(root, config, dsl);
    }

    // ======================================================
    // MAVEN
    // ======================================================

    private static void generateMaven(Path root,
                                      SbxConfig config) {

        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">

                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>%s</version>
                        <relativePath/>
                    </parent>

                    <groupId>io.github</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.0.1-SNAPSHOT</version>

                    <properties>
                        <java.version>%d</java.version>
                    </properties>

                    <dependencies>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>

                </project>
                """.formatted(
                config.getRuntime().getSpringBoot(),
                config.getProject().getName(),
                config.getRuntime().getJava()
        );

        write(root.resolve("pom.xml"), pom);
    }

    // ======================================================
    // GRADLE
    // ======================================================

    private static void generateGradle(Path root,
                                       SbxConfig config,
                                       String dsl) {

        boolean kotlin = "kotlin".equalsIgnoreCase(dsl);

        String boot = config.getRuntime().getSpringBoot();
        int java = config.getRuntime().getJava();

        String content = kotlin
                ? gradleKotlinTemplate(boot, java)
                : gradleGroovyTemplate(boot, java);

        Path file = kotlin
                ? root.resolve("build.gradle.kts")
                : root.resolve("build.gradle");

        write(file, content);
    }

    private static String gradleGroovyTemplate(String boot,
                                               int java) {

        return """
                plugins {
                    id 'java'
                    id 'org.springframework.boot' version '%s'
                    id 'io.spring.dependency-management' version '1.1.7'
                }

                group = 'io.github'
                version = '0.0.1-SNAPSHOT'

                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(%d)
                    }
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                }

                tasks.withType(Test) {
                    useJUnitPlatform()
                }
                """.formatted(boot, java);
    }

    private static String gradleKotlinTemplate(String boot,
                                               int java) {

        return """
                plugins {
                    java
                    id("org.springframework.boot") version "%s"
                    id("io.spring.dependency-management") version "1.1.7"
                }

                group = "io.github"
                version = "0.0.1-SNAPSHOT"

                java {
                    toolchain {
                        languageVersion.set(JavaLanguageVersion.of(%d))
                    }
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                }

                tasks.withType<Test> {
                    useJUnitPlatform()
                }
                """.formatted(boot, java);
    }

    // ======================================================

    private static void write(Path file, String content) {

        try {
            Files.writeString(file, content);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to generate " + file.getFileName(), e);
        }
    }
}