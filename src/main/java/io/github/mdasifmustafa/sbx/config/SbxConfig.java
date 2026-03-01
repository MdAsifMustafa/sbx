package io.github.mdasifmustafa.sbx.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SbxConfig {

    @JsonProperty("schema")
    private int schema;

    @JsonProperty("project")
    private ProjectConfig project;

    @JsonProperty("runtime")
    private RuntimeConfig runtime;

    private Map<String, DependencyConfig> dependencies;

    public int getSchema() {
        return schema;
    }

    public ProjectConfig getProject() {
        return project;
    }

    public RuntimeConfig getRuntime() {
        return runtime;
    }
    
    public Map<String, DependencyConfig> getDependencies() {
        return dependencies;
    }
}