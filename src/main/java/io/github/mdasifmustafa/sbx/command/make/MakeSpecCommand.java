package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "spec", description = "Create JPA Specification helper")
public class MakeSpecCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Specification name (e.g. User)")
    private String name;

    @Option(names = "--entity", description = "Entity class name")
    private String entity;

    @Option(names = "--paging", description = "Include paging helper")
    private boolean paging;

    @Option(names = "--sorting", description = "Include sorting helper")
    private boolean sorting;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".domain.spec";
        String className = normalize(name, "Specification");
        String entityName = entity != null && !entity.isBlank() ? entity : name;

        Path path = resolveJavaPath(pkg, className);
        String content = TemplateEngine.specification(pkg, className, entityName, paging, sorting);
        write(path, content, force, dryRun);
    }
}
