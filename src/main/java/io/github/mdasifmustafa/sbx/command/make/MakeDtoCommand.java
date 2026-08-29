package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "dto",
        description = "Create DTO"
)
public class MakeDtoCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Base name (e.g. User)")
    private String name;

    @Option(names = "--request", description = "Generate request DTO")
    private boolean request;

    @Option(names = "--response", description = "Generate response DTO")
    private boolean response;

    @Option(names = "--record", description = "Generate Java record")
    private boolean record;

    @Option(names = "--validation", description = "Add validation annotations")
    private boolean validation;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Option(names = "--from-entity", description = "Generate DTO from JPA entity")
    private boolean fromEntity;

    @Option(names = "--lombok", description = "Use Lombok annotations")
    private boolean lombok;

    @Option(names = "--package", description = "Relative package path (e.g. blog.posts or blog/posts)")
    private String customPackage;

    @Override
    public void run() {
        if (!ensureProject()) return;

        if (!isSimpleName(name)) {
            SbxResponse.error("❌ DTO name must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages.");
            return;
        }

        String entityName = toTitleCase(name);
        String basePackage = resolveBasePackage();
        String pkg = resolvePackage(basePackage, customPackage, "api.dto");
        String entityPackage = resolvePackage(basePackage, customPackage, "domain." + normalizeEntityPackageName(name));

        String dtoName = resolveDtoName(entityName);

        Path path = resolveJavaPath(pkg, dtoName);

        String content;

        if (fromEntity) {
          content = TemplateEngine.dtoFromEntity(
                pkg,
                dtoName,
                entityName,
                entityPackage,
                request,
                response,
                record,
                lombok
            );
        } else {
         content = TemplateEngine.dto(
                pkg,
                dtoName,
                record,
                validation
            );
        }

        write(path, content, force, dryRun);
    }

    private String normalizeEntityPackageName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return normalizePackage(value.replace('\\', '/').replace('.', '/'));
    }

    private String resolveDtoName(String entityName) {
        if (request) return entityName + "RequestDto";
        if (response) return entityName + "ResponseDto";
        return entityName + "Dto";
    }
}