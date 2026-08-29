package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.mapper.EntityMapperTemplate;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "mapper",
        description = "Create MapStruct mapper for entity"
)
public class MakeMapperCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Entity name (e.g. User)")
    private String name;

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
            SbxResponse.error("❌ Mapper name must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages.");
            return;
        }

        String entityName = toTitleCase(name);
        String basePackage = resolveBasePackage();
        String pkg = resolvePackage(basePackage, customPackage, "api.mapper");

        String mapperName = entityName + "Mapper";
        Path path = resolveJavaPath(pkg, mapperName);

        String content = EntityMapperTemplate.generate(pkg, entityName);

        write(path, content, force, dryRun);
    }
}