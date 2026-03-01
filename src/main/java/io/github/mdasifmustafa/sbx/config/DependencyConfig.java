package io.github.mdasifmustafa.sbx.config;

public class DependencyConfig {
	public String version;
	public String scope;
    // Maven specific
    public boolean optional;

    // Annotation processor support
    public boolean annotationProcessor;
    public String processorArtifact;
}