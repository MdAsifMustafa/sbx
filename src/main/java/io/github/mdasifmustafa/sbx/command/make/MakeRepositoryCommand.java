package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "repository",
        description = "Create repository"
)
public class MakeRepositoryCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Entity name (e.g. User)")
    private String name;

    @Option(names = "--custom", description = "Create non-JPA repository")
    private boolean custom;

    @Option(names = "--query", description = "Add query method (e.g. findByEmail)")
    private String query;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".domain." + name.toLowerCase();

        String repoName = name + "Repository";
        Path path = resolveJavaPath(pkg, repoName);

        String content = TemplateEngine.repository(
                pkg,
                name,
                custom,
                query
        );

        write(path, content, force, dryRun);
    }
}