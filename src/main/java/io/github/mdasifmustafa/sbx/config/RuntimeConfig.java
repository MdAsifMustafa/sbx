package io.github.mdasifmustafa.sbx.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuntimeConfig {

    @JsonProperty("java")
    private Integer java;

    @JsonProperty("springBoot")
    private String springBoot;

    @JsonProperty("build")
    private String build;
    
    @JsonProperty("configFormat")
    private String configFormat;;


    public Integer getJava() {
        return java;
    }

    public String getSpringBoot() {
        return springBoot;
    }

    public String getBuild() {
        return build;
    }
    
    public void setBuild(String build) {
        this.build = build;
    }
}