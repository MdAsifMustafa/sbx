package io.github.mdasifmustafa.sbx.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mdasifmustafa.sbx.config.SbxConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SbxConfigReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static SbxConfig read(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalStateException("sbx.json not found at: " + path.toAbsolutePath());
        }

        try {
            return mapper.readValue(path.toFile(), SbxConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read sbx.json: " + e.getMessage(), e);
        }
    }
}