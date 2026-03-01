package io.github.mdasifmustafa.sbx.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.mdasifmustafa.sbx.config.SbxConfig;

import java.io.IOException;
import java.nio.file.Path;

public class SbxConfigWriter {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void write(Path path, SbxConfig config) {
        try {
            mapper.writeValue(path.toFile(), config);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write sbx.json: " + e.getMessage(), e);
        }
    }
}

