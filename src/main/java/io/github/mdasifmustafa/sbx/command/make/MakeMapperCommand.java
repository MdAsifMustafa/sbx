package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.mapper.EntityMapperTemplate;
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

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".api.mapper";

        String mapperName = name + "Mapper";
        Path path = resolveJavaPath(pkg, mapperName);

        String content = EntityMapperTemplate.generate(pkg, name);

        write(path, content, force, dryRun);
    }
}