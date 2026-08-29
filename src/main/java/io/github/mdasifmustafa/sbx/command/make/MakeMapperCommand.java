package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

    @Option(names = "--dto", description = "DTO class name")
    private String dto;

    @Option(names = "--component-model", description = "MapStruct component model")
    private String componentModel = "spring";

    @Option(names = "--update-method", description = "Generate update method")
    private boolean updateMethod;

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

        String requestDtoName = entityName + "RequestDto";
        String responseDtoName = entityName + "ResponseDto";

        // If explicit --dto provided, use it as both request/response fallback
        if (dto != null && !dto.isBlank()) {
            requestDtoName = dto;
            responseDtoName = dto;
        }

        // Check if request/response DTO files exist; if not, fall back gracefully
        String basePkg = resolvePackage(resolveBasePackage(), customPackage, "");
        Path dtoRequestPath = Path.of("src/main/java", basePkg.replace('.', '/'), "api", "dto", requestDtoName + ".java");
        Path dtoResponsePath = Path.of("src/main/java", basePkg.replace('.', '/'), "api", "dto", responseDtoName + ".java");

        String req = Files.exists(dtoRequestPath) ? requestDtoName : "";
        String res = Files.exists(dtoResponsePath) ? responseDtoName : "";

        String content = TemplateEngine.mapstructMapper(pkg, entityName, req, res, componentModel, updateMethod);

        write(path, content, force, dryRun);
    }

    @Override
    protected String[] requiredDependencies() {
        return new String[]{"mapstruct", "mapstruct-processor"};
    }
}