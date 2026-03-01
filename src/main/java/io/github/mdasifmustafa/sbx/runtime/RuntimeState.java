package io.github.mdasifmustafa.sbx.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RuntimeState {

    private static final Path RUNTIME_FILE =
            Path.of(".sbx/runtime/app.runtime.json");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RuntimeState() {}

    public static void write(AppRuntimeInfo info) {
        try {
            Files.createDirectories(RUNTIME_FILE.getParent());
            MAPPER.writeValue(RUNTIME_FILE.toFile(), info);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write runtime state", e);
        }
    }

    public static AppRuntimeInfo read() {
        try {
            if (!Files.exists(RUNTIME_FILE)) {
                throw new IllegalStateException("Application is not running");
            }
            return MAPPER.readValue(RUNTIME_FILE.toFile(), AppRuntimeInfo.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read runtime state", e);
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(RUNTIME_FILE);
        } catch (IOException ignored) {}
    }
}