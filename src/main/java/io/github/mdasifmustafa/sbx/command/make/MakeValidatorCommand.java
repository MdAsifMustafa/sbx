package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "validator", description = "Create custom bean validator annotation and class")
public class MakeValidatorCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Validator name (e.g. StrongPassword)")
    private String name;

    @Option(names = "--field-type", description = "Validated field type")
    private String fieldType = "String";

    @Option(names = "--message", description = "Default validation message")
    private String message = "Invalid value";

    @Option(names = "--groups", description = "Generate groups hint comment")
    private boolean groups;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".validation";
        String annotationName = name;

        Path annotationPath = resolveJavaPath(pkg, annotationName);
        String annotationContent = TemplateEngine.validatorAnnotation(pkg, annotationName, message);
        if (groups) {
            annotationContent += "\n// groups option selected: define validation groups as needed\n";
        }
        write(annotationPath, annotationContent, force, dryRun);

        Path validatorPath = resolveJavaPath(pkg, annotationName + "Validator");
        String validatorContent = TemplateEngine.validatorClass(pkg, annotationName, fieldType);
        write(validatorPath, validatorContent, force, dryRun);
    }
}
