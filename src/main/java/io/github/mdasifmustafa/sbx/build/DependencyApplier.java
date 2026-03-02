package io.github.mdasifmustafa.sbx.build;

import java.nio.file.Path;
import io.github.mdasifmustafa.sbx.config.DependencyConfig;

public interface DependencyApplier {

    boolean supports(Path projectRoot);

    void apply(
        Path projectRoot,
        String coordinates,
        DependencyConfig dependency
    ) throws Exception;
    
    void remove(
            Path projectRoot,
            String coordinates
        ) throws Exception;
}