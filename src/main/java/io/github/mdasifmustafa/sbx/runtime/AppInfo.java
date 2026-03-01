package io.github.mdasifmustafa.sbx.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and provides access to SBX application metadata.
 */
public class AppInfo {

    private static final Logger logger = LoggerFactory.getLogger(AppInfo.class);
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppInfo.class.getClassLoader().getResourceAsStream("sbx.properties")) {
            if (in != null) {
                PROPS.load(in);
            } else {
                logger.warn("sbx.properties not found in classpath");
            }
        } catch (IOException e) {
            logger.error("Failed to load sbx.properties", e);
        }
    }

    private AppInfo() {}

    public static String getName() {
        return PROPS.getProperty("sbx.name", "SBX");
    }

    public static String getVersion() {
        return PROPS.getProperty("sbx.version", "unknown");
    }

    public static String getRepoOwner() {
        String owner = getFirstNonBlankProperty("sbx.repo.owner", "SBX.repo.owner");
        return owner == null ? "author" : owner;
    }

    public static String getRepoName() {
        return PROPS.getProperty("sbx.repo.name", "SBX");
    }

    public static String getAuthorDisplay() {
        String author = getFirstNonBlankProperty(
                "sbx.repo.ownername",
                "SBX.repo.ownername",
                "sbx.repo.owner");
        return author == null ? "author" : author;
    }

    public static String getRepoUrl() {
        String url = PROPS.getProperty("sbx.repo.url");
        if (url != null && !url.isBlank()) {
            return url;
        }

        String owner = getRepoOwner();
        String name = getRepoName();
        if (owner == null || owner.isBlank()
                || "author".equalsIgnoreCase(owner)
                || name == null || name.isBlank()
                || "SBX".equalsIgnoreCase(name)) {
            return "";
        }
        return String.format("https://github.com/%s/%s", owner, name);
    }

    private static String getFirstNonBlankProperty(String... keys) {
        for (String key : keys) {
            String value = PROPS.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public static void printBanner() {
        String name = getName();
        String version = getVersion();
        String author = getAuthorDisplay();
        String repoUrl = getRepoUrl();

        String blue = "\u001B[34m";
        String cyan = "\u001B[36m";
        String reset = "\u001B[0m";

        String title = String.format("%s CLI  |  Spring Boot eXperience", name.toUpperCase());

        List<String> lines = new ArrayList<>();
        lines.add(String.format("Version : %s", version));
        lines.add(String.format("Author  : %s", author));
        if (!repoUrl.isBlank()) {
            lines.add(String.format("Repo    : %s", repoUrl));
        }

        int contentWidth = Math.max(
                title.length(),
                lines.stream().mapToInt(String::length).max().orElse(0)) + 4;
        int minContentWidth = 60;
        if (contentWidth < minContentWidth) {
            contentWidth = minContentWidth;
        }

        String border = "+" + "=".repeat(contentWidth) + "+";
        String separator = "|" + "-".repeat(contentWidth) + "|";
        System.out.println();
        System.out.println(blue + "    " + border + reset);
        System.out.println(blue + "    |" + reset + cyan + centerText(title, contentWidth) + reset + blue + "|" + reset);
        System.out.println(blue + "    " + separator + reset);
        for (String line : lines) {
            String text = "  " + line;
            System.out.println(
                    blue + "    |" + reset
                    + cyan + padRight(text, contentWidth) + reset
                    + blue + "|" + reset);
        }
        System.out.println(blue + "    " + border + reset);
        System.out.println();
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int leftPad = (width - text.length()) / 2;
        int rightPad = width - text.length() - leftPad;
        return " ".repeat(leftPad) + text + " ".repeat(rightPad);
    }
}
