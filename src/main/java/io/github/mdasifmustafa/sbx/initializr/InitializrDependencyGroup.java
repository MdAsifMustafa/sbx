package io.github.mdasifmustafa.sbx.initializr;

import java.util.List;

public record InitializrDependencyGroup(
        String name,
        List<InitializrDependency> values
) {}