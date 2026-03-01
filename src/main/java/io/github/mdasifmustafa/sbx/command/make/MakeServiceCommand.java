package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;

@Command(
        name = "service",
        description = "Create service interface and implementation"
)
public class MakeServiceCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Service name (e.g. User)")
    private String name;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;
    
    @Option(names = "--interface", description = "Generate only service interface")
    private boolean onlyInterface;

    @Option(names = "--impl", description = "Generate only service implementation")
    private boolean onlyImpl;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".service";

        String baseName = name;

        boolean generateInterface = !onlyImpl;
        boolean generateImpl = !onlyInterface;

        // safety check
        if (!generateInterface && !generateImpl) {
            System.err.println("❌ Nothing to generate. Choose --interface or --impl.");
            return;
        }

        if (generateInterface) {
            Path interfacePath = resolveJavaPath(pkg, baseName + "Service");
            write(
                interfacePath,
                TemplateEngine.serviceInterface(pkg, baseName),
                force,
                dryRun
            );
        }

        if (generateImpl) {
            Path implPath = resolveJavaPath(pkg, baseName + "ServiceImpl");
            write(
                implPath,
                TemplateEngine.serviceImpl(pkg, baseName),
                force,
                dryRun
            );
        }
    }
}