package io.github.mdasifmustafa.sbx.ux;

import java.util.List;

public class SbxOptionSection {

    private final String name;
    private final List<String> options;

    public SbxOptionSection(String name, List<String> options) {
        this.name = name;
        this.options = options;
    }

    public String name() {
        return name;
    }

    public List<String> options() {
        return options;
    }
}