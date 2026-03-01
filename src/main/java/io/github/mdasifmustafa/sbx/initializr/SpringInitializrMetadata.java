package io.github.mdasifmustafa.sbx.initializr;

public record SpringInitializrMetadata(
        InitializrSingleSelect type,        // 👈 build tool
        InitializrSingleSelect packaging,
        InitializrSingleSelect javaVersion,
        InitializrSingleSelect language,
        InitializrSingleSelect bootVersion
) {}