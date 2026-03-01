package io.github.mdasifmustafa.sbx.runtime;

public record AppRuntimeInfo(
        long pid,
        String host,
        int port,
        String actuatorPath
) {}