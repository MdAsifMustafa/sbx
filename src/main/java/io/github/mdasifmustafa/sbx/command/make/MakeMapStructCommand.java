package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "mapstruct", description = "Create MapStruct mapper scaffold")
public class MakeMapStructCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Entity name (e.g. User)")
    private String entity;

    @Option(names = "--dto", description = "DTO class name")
    private String dto;

    @Option(names = "--component-model", description = "MapStruct component model")
    private String componentModel = "spring";

    @Option(names = "--update-method", description = "Generate update method")
    private boolean updateMethod;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".api.mapper";
        String dtoName = dto != null && !dto.isBlank() ? dto : entity + "Dto";

        Path path = resolveJavaPath(pkg, entity + "MapStructMapper");
        String content = TemplateEngine.mapstructMapper(pkg, entity, dtoName, componentModel, updateMethod);
        write(path, content, force, dryRun);
    }
}
