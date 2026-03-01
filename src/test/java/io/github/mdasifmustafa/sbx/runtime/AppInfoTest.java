package io.github.mdasifmustafa.sbx.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppInfoTest {

    private Properties props;
    private Properties originalProps;

    @BeforeEach
    void setUp() throws Exception {
        Field propsField = AppInfo.class.getDeclaredField("PROPS");
        propsField.setAccessible(true);
        props = (Properties) propsField.get(null);
        originalProps = new Properties();
        originalProps.putAll(props);
        props.clear();
    }

    @AfterEach
    void tearDown() {
        props.clear();
        props.putAll(originalProps);
    }

    @Test
    void getRepoUrlUsesExplicitPropertyWhenPresent() {
        props.setProperty("sbx.repo.url", "https://example.com/custom/repo");
        props.setProperty("sbx.repo.owner", "alice");
        props.setProperty("sbx.repo.name", "demo");

        assertEquals("https://example.com/custom/repo", AppInfo.getRepoUrl());
    }

    @Test
    void getRepoUrlBuildsFallbackFromOwnerAndName() {
        props.setProperty("sbx.repo.owner", "alice");
        props.setProperty("sbx.repo.name", "demo");

        assertEquals("https://github.com/alice/demo", AppInfo.getRepoUrl());
    }

    @Test
    void printBannerShowsDynamicContentAndConditionalRepoRow() {
        props.setProperty("sbx.name", "sbx");
        props.setProperty("sbx.version", "0.1.0-SNAPSHOT");
        props.setProperty("sbx.repo.ownername", "Md Asif Mustafa");
        props.setProperty("sbx.repo.url", "https://github.com/mdasifmustafa/sbx");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            AppInfo.printBanner();
        } finally {
            System.setOut(original);
        }

        String rendered = out.toString(StandardCharsets.UTF_8);
        assertTrue(rendered.contains("SBX CLI  |  Spring Boot eXperience"));
        assertTrue(rendered.contains("Version : 0.1.0-SNAPSHOT"));
        assertTrue(rendered.contains("Author  : Md Asif Mustafa"));
        assertTrue(rendered.contains("Repo    : https://github.com/mdasifmustafa/sbx"));

        props.remove("sbx.repo.url");
        out.reset();
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            AppInfo.printBanner();
        } finally {
            System.setOut(original);
        }

        rendered = out.toString(StandardCharsets.UTF_8);
        assertFalse(rendered.contains("Repo    :"));
    }
}
