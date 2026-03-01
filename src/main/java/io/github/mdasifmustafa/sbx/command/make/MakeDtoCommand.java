package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
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
    
    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".api.dto";

        String dtoName = resolveDtoName();

        Path path = resolveJavaPath(pkg, dtoName);
        
        String content;

        if (fromEntity) {
          content = TemplateEngine.dtoFromEntity(
                pkg,
                dtoName,
                name,        // Entity name
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

    private String resolveDtoName() {
        if (request) return name + "RequestDto";
        if (response) return name + "ResponseDto";
        return name + "Dto";
    }
}