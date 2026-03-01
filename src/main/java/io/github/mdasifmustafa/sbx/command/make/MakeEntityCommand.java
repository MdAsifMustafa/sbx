package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "entity",
        description = "Create JPA entity"
)
public class MakeEntityCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Entity name (e.g. User)")
    private String name;

    @Option(names = "--table", description = "Custom table name")
    private String table;
    

    @Option(names = "--lombok", description = "Add getters and setters")
    private boolean lombok;

    @Option(names = "--uuid", description = "Use UUID as primary key")
    private boolean uuid;

    @Option(names = "--auditable", description = "Add createdAt / updatedAt")
    private boolean auditable;

    @Option(names = "--soft-delete", description = "Add deletedAt field")
    private boolean softDelete;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".domain." + name.toLowerCase();

        Path path = resolveJavaPath(pkg, name);

        String content = TemplateEngine.entity(
                pkg,
                name,
                table,
                uuid,
                auditable,
                softDelete,
                lombok
        );

        write(path, content, force, dryRun);
    }
}