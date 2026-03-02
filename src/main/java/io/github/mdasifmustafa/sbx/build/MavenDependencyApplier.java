package io.github.mdasifmustafa.sbx.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;

public class MavenDependencyApplier implements DependencyApplier {

    @Override
    public boolean supports(Path root) {
        return Files.exists(root.resolve("pom.xml"));
    }

    @Override
    public void apply(Path root, String coords, DependencyConfig dep) throws Exception {

        Path pom = root.resolve("pom.xml");

        String[] parts = coords.split(":");
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = dep.version;

        String xml = Files.readString(pom);

        boolean isProcessor = "annotationProcessor".equals(dep.scope);

        // --------------------------------------------------
        // Insert dependency only if NOT annotationProcessor
        // --------------------------------------------------

        if (!isProcessor) {

            boolean exists = dependencyExists(xml, groupId, artifactId);

            if (!exists) {
                String dependencySnippet =
                        buildDependencySnippet(groupId, artifactId, version, dep);

                xml = insertDependency(xml, dependencySnippet);
            }
        }

        // --------------------------------------------------
        // Always handle processor paths separately
        // --------------------------------------------------

        if (isProcessor) {
            xml = insertAnnotationProcessor(
                    xml,
                    groupId,
                    artifactId,
                    version
            );
        }

        Files.writeString(pom, xml);
    }

    // ======================================================
    // DEPENDENCY INSERTION
    // ======================================================

    private String insertDependency(String xml, String snippet) {

        Pattern pattern = Pattern.compile(
                "<dependencies>(.*?)</dependencies>",
                Pattern.DOTALL
        );

        Matcher m = pattern.matcher(xml);

        if (m.find()) {

            String block = m.group(1);

            String indent = detectIndent(block);

            String newBlock =
                    "<dependencies>" +
                    block +
                    "\n" + indent + snippet +
                    "\n" +
                    indent.substring(0, Math.max(0, indent.length() - 2)) +
                    "</dependencies>";

            return xml.replace(m.group(0), newBlock);
        }

        // create dependencies section under project
        String deps =
                "\n    <dependencies>\n" +
                "        " + snippet + "\n" +
                "    </dependencies>\n";

        return xml.replaceFirst(
                "</project>",
                deps + "</project>"
        );
    }

    private String buildDependencySnippet(String g,
                                          String a,
                                          String v,
                                          DependencyConfig dep) {

        StringBuilder sb = new StringBuilder();

        sb.append("<dependency>\n");
        sb.append("            <groupId>").append(g).append("</groupId>\n");
        sb.append("            <artifactId>").append(a).append("</artifactId>\n");

        if (v != null && !isSpringBootStarter(g)) {
            sb.append("            <version>").append(v).append("</version>\n");
        }

        if (dep.scope != null &&
            !"implementation".equals(dep.scope)) {

            sb.append("            <scope>")
              .append(mapScope(dep.scope))
              .append("</scope>\n");
        }

        if (dep.optional) {
            sb.append("            <optional>true</optional>\n");
        }

        sb.append("        </dependency>");

        return sb.toString();
    }

    private boolean dependencyExists(String xml,
                                     String groupId,
                                     String artifactId) {

        String pattern =
                "<groupId>" + Pattern.quote(groupId) + "</groupId>\\s*" +
                "<artifactId>" + Pattern.quote(artifactId) + "</artifactId>";

        return Pattern.compile(pattern, Pattern.DOTALL)
                .matcher(xml)
                .find();
    }

    // ======================================================
    // ANNOTATION PROCESSOR
    // ======================================================

    private String insertAnnotationProcessor(String xml,
                                             String groupId,
                                             String artifactId,
                                             String version) {

        if (!xml.contains("<build>")) {

            String build =
                    "\n    <build>\n" +
                    "        <plugins>\n" +
                    compilerPluginSnippet(groupId, artifactId, version) +
                    "        </plugins>\n" +
                    "    </build>\n";

            return xml.replace("</project>", build + "</project>");
        }

        if (!xml.contains("maven-compiler-plugin")) {

            return xml.replace(
                    "<plugins>",
                    "<plugins>\n" +
                    compilerPluginSnippet(groupId, artifactId, version)
            );
        }

        // plugin exists → append path
        String pathSnippet =
                "                    <path>\n" +
                "                        <groupId>" + groupId + "</groupId>\n" +
                "                        <artifactId>" + artifactId + "</artifactId>\n" +
                (version != null
                        ? "                        <version>" + version + "</version>\n"
                        : "") +
                "                    </path>\n";

        return xml.replace(
                "</annotationProcessorPaths>",
                pathSnippet + "                </annotationProcessorPaths>"
        );
    }

    private String compilerPluginSnippet(String g,
                                         String a,
                                         String v) {

        return
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <configuration>\n" +
                "                    <annotationProcessorPaths>\n" +
                "                        <path>\n" +
                "                            <groupId>" + g + "</groupId>\n" +
                "                            <artifactId>" + a + "</artifactId>\n" +
                (v != null
                        ? "                            <version>" + v + "</version>\n"
                        : "") +
                "                        </path>\n" +
                "                    </annotationProcessorPaths>\n" +
                "                </configuration>\n" +
                "            </plugin>\n";
    }

    private String detectIndent(String text) {

        Pattern p = Pattern.compile("\n(\\s*)<");
        Matcher m = p.matcher(text);

        if (m.find()) {
            return m.group(1);
        }

        return "        ";
    }

    private String mapScope(String scope) {
        return switch (scope) {
            case "test" -> "test";
            case "runtime" -> "runtime";
            case "compileOnly" -> "provided";
            default -> scope;
        };
    }

    @Override
    public void remove(Path root, String coords) throws Exception {

        Path pom = root.resolve("pom.xml");

        if (!Files.exists(pom)) return;

        String[] parts = coords.split(":");

        if (parts.length < 2) return;

        String groupId = parts[0];
        String artifactId = parts[1];

        String xml = Files.readString(pom);

        xml = removeDependencyBlock(xml, groupId, artifactId);

        xml = removeProcessorPath(xml, groupId, artifactId);

        Files.writeString(pom, xml);
    }
    private String removeDependencyBlock(String xml,
		            String groupId,
		            String artifactId) {
		
		Pattern p = Pattern.compile("(?s)<dependency>.*?</dependency>");
		Matcher m = p.matcher(xml);
		
		StringBuffer result = new StringBuffer();
		
		while (m.find()) {
		
		String block = m.group();
		
		if (block.contains("<groupId>" + groupId + "</groupId>")
		&& block.contains("<artifactId>" + artifactId + "</artifactId>")) {
		
		m.appendReplacement(result, ""); // remove only this one
		
		} else {
		m.appendReplacement(result,
		Matcher.quoteReplacement(block));
		}
		}
		
		m.appendTail(result);
		
		return result.toString();
		}
    private String removeProcessorPath(String xml,
		            String groupId,
		            String artifactId) {
		
		Pattern p = Pattern.compile("(?s)<path>.*?</path>");
		Matcher m = p.matcher(xml);
		
		StringBuffer result = new StringBuffer();
		
		while (m.find()) {
		
		String block = m.group();
		
		if (block.contains("<groupId>" + groupId + "</groupId>")
		&& block.contains("<artifactId>" + artifactId + "</artifactId>")) {
		
		m.appendReplacement(result, "");
		
		} else {
		m.appendReplacement(result,
		Matcher.quoteReplacement(block));
		}
		}
		
		m.appendTail(result);
		
		return result.toString();
		}
    private boolean isSpringBootStarter(String groupId) {
        return "org.springframework.boot".equals(groupId);
    }
}