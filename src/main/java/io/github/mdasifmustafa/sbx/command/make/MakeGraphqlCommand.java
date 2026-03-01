package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "graphql",
        description = "Create GraphQL resolver and schema"
)
public class MakeGraphqlCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Base name (e.g. User)")
    private String name;

    @Option(names = "--query", description = "Generate query")
    private boolean query;

    @Option(names = "--mutation", description = "Generate mutation")
    private boolean mutation;

    @Option(names = "--schema", description = "Generate schema only")
    private boolean schemaOnly;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        // default behavior
        if (!query && !mutation) {
            query = true;
        }

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".api.graphql." + name.toLowerCase();

        if (!schemaOnly) {
            Path resolverPath = resolveJavaPath(pkg, name + "Resolver");

            String resolverContent = TemplateEngine.graphqlResolver(
                    pkg,
                    name,
                    query,
                    mutation
            );

            write(resolverPath, resolverContent, force, dryRun);
        }

        Path schemaPath = Path.of(
                "src/main/resources/graphql",
                name.toLowerCase() + ".graphqls"
        );

        String schemaContent = TemplateEngine.graphqlSchema(
                name,
                query,
                mutation
        );

        write(schemaPath, schemaContent, force, dryRun);
    }
}