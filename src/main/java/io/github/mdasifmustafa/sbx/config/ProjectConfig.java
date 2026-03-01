package io.github.mdasifmustafa.sbx.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}