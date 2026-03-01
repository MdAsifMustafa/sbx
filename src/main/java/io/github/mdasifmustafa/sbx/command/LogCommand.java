package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Command(
    name = "log",
    description = "Show application logs"
)
public class LogCommand implements Runnable {

    @Option(
        names = {"-f", "--follow"},
        description = "Follow log output"
    )
    private boolean follow;

    @Option(
        names = "--tail",
        description = "Number of lines to show from the end of the log"
    )
    private Integer tail;

    @Option(
        names = "--since",
        description = "Show logs modified since duration (e.g. 5m, 30s, 1h)"
    )
    private String since;

    @Override
    public void run() {
        Path logFile = BuildExecutor.logFile();

        if (!Files.exists(logFile)) {
            System.err.println("❌ No logs found. Is the application running?");
            return;
        }

        if (!passesSinceFilter(logFile)) {
            System.out.println("ℹ️ No logs since " + since);
            return;
        }

        print(logFile);

        if (follow) {
            follow(logFile);
        }
    }

    /* ===================== printing ===================== */

    private void print(Path logFile) {
        try {
            List<String> lines = Files.readAllLines(logFile);

            if (tail != null && tail < lines.size()) {
                lines = lines.subList(lines.size() - tail, lines.size());
            }

            lines.forEach(System.out::println);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read log file", e);
        }
    }

    /* ===================== follow ===================== */

    private void follow(Path logFile) {
        System.out.println("\n📜 Following logs (Ctrl+C to stop)\n");

        try {
            long lastSize = Files.size(logFile);

            while (true) {
                long size = Files.size(logFile);

                if (size > lastSize) {
                    List<String> lines = Files.readAllLines(logFile);
                    lines.forEach(System.out::println);
                    lastSize = size;
                }

                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Log follow interrupted", e);
        }
    }

    /* ===================== since ===================== */

    private boolean passesSinceFilter(Path logFile) {
        if (since == null) {
            return true;
        }

        try {
            Instant cutoff = Instant.now().minus(parseDuration(since));
            Instant modified = Files.getLastModifiedTime(logFile).toInstant();
            return modified.isAfter(cutoff);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read log metadata", e);
        }
    }

    private Duration parseDuration(String value) {
        if (value.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(value.replace("s", "")));
        }
        if (value.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(value.replace("m", "")));
        }
        if (value.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(value.replace("h", "")));
        }
        throw new IllegalArgumentException("Invalid duration: " + value);
    }
}