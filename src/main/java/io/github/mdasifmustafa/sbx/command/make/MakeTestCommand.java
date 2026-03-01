package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "test", description = "Create test class scaffold")
public class MakeTestCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Test name (e.g. UserService)")
    private String name;

    @Option(names = "--type", description = "Test type (unit|integration|webmvc|datajpa)")
    private String type = "unit";

    @Option(names = "--mockito", description = "Include Mockito static import")
    private boolean mockito;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage;
        String className = normalize(name, "Test");

        Path path = Path.of("src/test/java", pkg.replace('.', '/'), className + ".java");
        String content = TemplateEngine.testClass(pkg, className, type, mockito);
        write(path, content, force, dryRun);
    }
}
