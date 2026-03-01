package io.github.mdasifmustafa.sbx.io;

import io.github.mdasifmustafa.sbx.config.SbxConfig;

public class SbxConfigValidator {

    public static void validate(SbxConfig config) {
        if (config == null) {
            throw new IllegalStateException("sbx.json is empty or malformed");
        }

        if (config.getSchema() != 1) {
            throw new IllegalStateException("Unsupported sbx.json schema: " + config.getSchema());
        }

        if (config.getProject() == null) {
            throw new IllegalStateException("Missing required section: project");
        }

        if (config.getRuntime() == null) {
            throw new IllegalStateException("Missing required section: runtime");
        }
    }
}