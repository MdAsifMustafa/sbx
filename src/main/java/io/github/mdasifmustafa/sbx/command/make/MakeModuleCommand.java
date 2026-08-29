package io.github.mdasifmustafa.sbx.command.make;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import io.github.mdasifmustafa.sbx.ux.SbxLogger;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "module", description = "Create module package structure")
public class MakeModuleCommand extends AbstractMakeCommand {

    private static final Pattern VALID_MODULE_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9]*$");

    @Parameters(index = "0", description = "Module name (e.g. Billing)")
    private String name;

    @Option(names = "--with-crud", description = "Generate CRUD placeholder packages")
    private boolean withCrud;

    @Option(names = "--with-event", description = "Generate event package")
    private boolean withEvent;

    @Option(names = "--with-mail", description = "Generate mail package")
    private boolean withMail;

    @Option(names = "--force", description = "Overwrite module README and allow creation outside a multi-module project")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Option(names = "--package", description = "Relative package path under the base package (e.g. blog.posts or blog/posts)")
    private String customPackage;

    public static boolean isValidModuleName(String value) {
        return value != null && VALID_MODULE_NAME.matcher(value.trim()).matches();
    }

    private static void fail(String message) {
        SbxLogger.error(Path.of("."), message);
        throw new IllegalArgumentException(message);
    }

    public static void addModuleToParentPom(Path pomPath, String moduleName) throws IOException {
        String pom = Files.readString(pomPath);
        String normalizedModule = moduleName.trim();

        if (!isValidModuleName(normalizedModule)) {
            throw new IllegalArgumentException("Module name must contain only letters and numbers, e.g. blog, Blog, or BlogPost");
        }

        if (pom.contains("<module>" + normalizedModule + "</module>")) {
            return;
        }

        String lowerModule = normalizedModule.toLowerCase(Locale.ROOT);
        String updatedPom;
        if (pom.contains("<modules>")) {
            String modulesBlock = extractTagContent(pom, "<modules>", "</modules>");
            List<String> moduleEntries = new ArrayList<>();
            if (modulesBlock != null && !modulesBlock.isBlank()) {
                Matcher matcher = Pattern.compile("<module>(.*?)</module>").matcher(modulesBlock);
                while (matcher.find()) {
                    moduleEntries.add(matcher.group(1).trim());
                }
            }
            if (moduleEntries.stream().noneMatch(m -> m.equalsIgnoreCase(normalizedModule))) {
                moduleEntries.add(normalizedModule);
            }
            String joined = String.join("\n        ",
                    moduleEntries.stream().map(m -> "<module>" + m + "</module>").toList());
            updatedPom = pom.replace(
                    extractTag(pom, "<modules>", "</modules>"),
                    "<modules>\n        " + joined + "\n    </modules>"
            );
        } else {
            updatedPom = pom.replace(
                    "</project>",
                    "    <modules>\n        <module>" + normalizedModule + "</module>\n    </modules>\n</project>"
            );
        }

        if (updatedPom.contains("<artifactId>" + lowerModule + "</artifactId>")) {
            Files.writeString(pomPath, updatedPom);
            return;
        }

        String artifactIdInsert = "    <dependency>\n"
                + "        <groupId>${project.groupId}</groupId>\n"
                + "        <artifactId>" + lowerModule + "</artifactId>\n"
                + "        <version>${project.version}</version>\n"
                + "    </dependency>\n";

        String dependencyBlock = "<dependencies>";
        if (updatedPom.contains(dependencyBlock)) {
            updatedPom = updatedPom.replace(
                    "</dependencies>",
                    artifactIdInsert + "</dependencies>"
            );
        } else {
            updatedPom = updatedPom.replace(
                    "</project>",
                    "    <dependencies>\n" + artifactIdInsert + "    </dependencies>\n</project>"
            );
        }

        Files.writeString(pomPath, updatedPom);
    }

    private static String extractTag(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex);
        if (startIndex < 0 || endIndex < 0) {
            return "";
        }
        return text.substring(startIndex, endIndex + end.length());
    }

    private static String extractTagContent(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex);
        if (startIndex < 0 || endIndex < 0) {
            return "";
        }
        return text.substring(startIndex + start.length(), endIndex);
    }

    @Override
    public void run() {
        if (!ensureProject()) return;

        if (!isValidModuleName(name)) {
            fail("Invalid module name '" + name + "'. Use letters and numbers only, e.g. blog, Blog, or BlogPost.");
        }

        Path root = Path.of("").toAbsolutePath();
        Path pomPath = root.resolve("pom.xml");
        boolean isMultiModule = Files.exists(pomPath) && hasModules(pomPath);

        if (!isMultiModule && !force) {
            SbxResponse.error("make module requires a multi-module parent project");
            SbxResponse.tip("Re-run with --force to allow generation in a single-module project.");
            return;
        }

        String basePackage = resolveBasePackage();
        String modulePkg = resolvePackage(basePackage, customPackage, name.toLowerCase(Locale.ROOT));

        if (Files.exists(pomPath) && isMultiModule) {
            try {
                addModuleToParentPom(pomPath, name);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to update parent pom.xml: " + e.getMessage(), e);
            }
        }

        Path marker = resolveJavaPath(modulePkg, "package-info");
        write(marker, "package " + modulePkg + ";\n", force, dryRun);

        if (withCrud) {
            write(resolveJavaPath(modulePkg + ".domain", "package-info"), "package " + modulePkg + ".domain;\n", force, dryRun);
            write(resolveJavaPath(modulePkg + ".api", "package-info"), "package " + modulePkg + ".api;\n", force, dryRun);
            write(resolveJavaPath(modulePkg + ".service", "package-info"), "package " + modulePkg + ".service;\n", force, dryRun);
        }
        if (withEvent) {
            write(resolveJavaPath(modulePkg + ".event", "package-info"), "package " + modulePkg + ".event;\n", force, dryRun);
        }
        if (withMail) {
            write(resolveJavaPath(modulePkg + ".mail", "package-info"), "package " + modulePkg + ".mail;\n", force, dryRun);
        }

        Path readmePath = Path.of("src/main/java", modulePkg.replace('.', '/'), "README.md");
        String readme = TemplateEngine.moduleReadme(name, withCrud, withEvent, withMail);
        write(readmePath, readme, force, dryRun);
    }

    private boolean hasModules(Path pomPath) {
        try {
            String pom = Files.readString(pomPath);
            return pom.contains("<modules>") && pom.contains("</modules>");
        } catch (IOException e) {
            return false;
        }
    }
}
