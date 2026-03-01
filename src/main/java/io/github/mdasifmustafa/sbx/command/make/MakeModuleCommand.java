package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "module", description = "Create module package structure")
public class MakeModuleCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Module name (e.g. Billing)")
    private String name;

    @Option(names = "--with-crud", description = "Generate CRUD placeholder packages")
    private boolean withCrud;

    @Option(names = "--with-event", description = "Generate event package")
    private boolean withEvent;

    @Option(names = "--with-mail", description = "Generate mail package")
    private boolean withMail;

    @Option(names = "--force", description = "Overwrite module README")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String modulePkg = basePackage + ".module." + name.toLowerCase();

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
}
