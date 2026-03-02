package io.github.mdasifmustafa.sbx.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;

public class GradleDependencyApplier implements DependencyApplier {

    @Override
    public boolean supports(Path root) {
        return Files.exists(root.resolve("build.gradle")) ||
               Files.exists(root.resolve("build.gradle.kts"));
    }

    @Override
    public void apply(Path root,
                      String coords,
                      DependencyConfig dep) throws Exception {

        Path file = resolveGradleFile(root);

        String content = Files.readString(file);

        String[] p = coords.split(":");

        String groupId = p[0];
        String artifactId = p[1];

        boolean bootStarter = "org.springframework.boot".equals(groupId);
        boolean kts = isKotlinDsl(file);

        String coordsStr =
                groupId + ":" + artifactId +
                (dep.version != null && !bootStarter
                        ? ":" + dep.version
                        : "");

        String notation = kts
                ? "(\"" + coordsStr + "\")"
                : " \"" + coordsStr + "\"";

        String config = mapScope(dep.scope);

        String line = "    " + config + notation;

        // --------------------------------------------------
        // Ensure dependencies block exists
        // --------------------------------------------------

        Pattern depsPattern = Pattern.compile("dependencies\\s*\\{");
        Matcher matcher = depsPattern.matcher(content);

        if (!matcher.find()) {
            content += "\ndependencies {\n}\n";
            matcher = depsPattern.matcher(content);
            matcher.find();
        }

        // --------------------------------------------------
        // Insert dependency line if not present
        // --------------------------------------------------

        if (!content.contains(line)) {

            int insertPos = matcher.end();

            content =
                    content.substring(0, insertPos) +
                    "\n" + line +
                    content.substring(insertPos);
        }

        Files.writeString(file, content);
    }

    @Override
    public void remove(Path root, String coords) throws Exception {

        Path gradle = resolveGradleFile(root);

        if (gradle == null) return;

        String[] parts = coords.split(":");

        if (parts.length < 2) return;

        String artifactId = parts[1];

        String content = Files.readString(gradle);

        content = content.replaceAll(
                ".*" + Pattern.quote(artifactId) + ".*\\n",
                ""
        );

        Files.writeString(gradle, content);
    }

    private Path resolveGradleFile(Path root) {

        if (Files.exists(root.resolve("build.gradle")))
            return root.resolve("build.gradle");

        return root.resolve("build.gradle.kts");
    }

    private boolean isKotlinDsl(Path gradleFile) {
        return gradleFile.getFileName()
                .toString()
                .endsWith(".kts");
    }

    private String mapScope(String scope) {

        if (scope == null) return "implementation";

        return switch (scope) {
            case "test" -> "testImplementation";
            case "runtime" -> "runtimeOnly";
            case "compileOnly" -> "compileOnly";
            case "annotationProcessor" -> "annotationProcessor";
            default -> "implementation";
        };
    }
}