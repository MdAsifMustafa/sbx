package io.github.mdasifmustafa.sbx.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BuildExecutor {

    private static final Path RUNTIME_DIR = Path.of(".sbx/runtime");
    private static final Path PID_FILE = RUNTIME_DIR.resolve("app.pid");
    private static final Path LOG_FILE = RUNTIME_DIR.resolve("app.log");

    public static void run(List<String> command) {
        run(command, false);
    }

    public static void run(List<String> command, boolean daemon) {
        try {
            Files.createDirectories(RUNTIME_DIR);

            ProcessBuilder pb = new ProcessBuilder(command);

            // ⭐ THIS IS THE BUG FIX
            pb.directory(Path.of("").toAbsolutePath().toFile());

            if (daemon) {
                pb.redirectOutput(LOG_FILE.toFile());
                pb.redirectError(LOG_FILE.toFile());
            } else {
                pb.inheritIO();
            }

            Process process = pb.start();

            if (daemon) {
                writePid(process.pid());
                System.out.println("✅ Application started in daemon mode (PID: " + process.pid() + ")");
                return;
            }

            int exit = process.waitFor();
            if (exit != 0) {
                throw new IllegalStateException("Command failed with exit code " + exit);
            }

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Execution failed: " + e.getMessage(), e);
        }
    }

    /* ===== PID helpers ===== */

    public static long readPid() throws IOException {
        if (!Files.exists(PID_FILE)) {
            throw new IllegalStateException("Application is not running");
        }
        return Long.parseLong(Files.readString(PID_FILE).trim());
    }

    public static void clearPid() throws IOException {
        Files.deleteIfExists(PID_FILE);
    }

    private static void writePid(long pid) throws IOException {
        Files.writeString(PID_FILE, String.valueOf(pid));
    }

    /* ===== Log helpers ===== */

    public static Path logFile() {
        return LOG_FILE;
    }
}