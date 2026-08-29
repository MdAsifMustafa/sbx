package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
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

    @Option(names = "--package", description = "Relative package path (e.g. blog.posts or blog/posts)")
    private String customPackage;

    @Override
    public void run() {
        if (!ensureProject()) return;

        if (!isSimpleName(name)) {
            SbxResponse.error("❌ Repository name must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages.");
            return;
        }

        String entityName = toTitleCase(name);
        String basePackage = resolveBasePackage();
        String pkg = resolvePackage(
                basePackage,
                customPackage,
                "domain." + name.toLowerCase()
        );

        String repoName = entityName + "Repository";
        Path path = resolveJavaPath(pkg, repoName);

        String content = TemplateEngine.repository(
                pkg,
                entityName,
                custom,
                query
        );

        write(path, content, force, dryRun);
    }
}