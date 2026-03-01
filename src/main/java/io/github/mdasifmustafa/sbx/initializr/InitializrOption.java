package io.github.mdasifmustafa.sbx.initializr;

public record InitializrOption(
        String id,
        String name,
        String action,
        boolean isDefault
) {}