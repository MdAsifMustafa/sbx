package io.github.mdasifmustafa.sbx.dependency;

public class MavenArtifact {

    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String description;

    public MavenArtifact(String groupId,
                         String artifactId,
                         String version,
                         String description) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.description = description;
    }

    public String coords() {
        if (version == null || version.isBlank()) {
            return groupId + ":" + artifactId;
        }
        return groupId + ":" + artifactId + ":" + version;
    }
}