package io.github.mdasifmustafa.sbx.dependency;

public class DependencyRule {

    public String groupId;
    public String artifactId;

    public String defaultScope;
    public boolean optional;

    public boolean annotationProcessor;
    public String processorArtifact;
}