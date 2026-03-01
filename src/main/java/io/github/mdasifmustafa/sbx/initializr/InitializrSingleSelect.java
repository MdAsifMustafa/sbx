package io.github.mdasifmustafa.sbx.initializr;

import java.util.List;

public record InitializrSingleSelect(
        String name,
        String defaultId,
        List<InitializrOption> values
) {
    public InitializrOption defaultOption() {
        return values.stream()
                .filter(InitializrOption::isDefault)
                .findFirst()
                .orElseThrow();
    }
    public InitializrSingleSelect onlyStarterZip() {
        List<InitializrOption> filtered =
                values.stream()
                      .filter(o -> "/starter.zip".equals(o.action()))
                      .toList();

        if (filtered.isEmpty()) {
            throw new IllegalStateException(
                    "No Spring Initializr project types available for /starter.zip"
            );
        }

        return new InitializrSingleSelect(name, defaultId, filtered);
    }
}