package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "exception", description = "Create custom exception and optional handler")
public class MakeExceptionCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Exception name (e.g. UserNotFound)")
    private String name;

    @Option(names = "--status", description = "HTTP status code for handler")
    private int status = 400;

    @Option(names = "--code", description = "Error code constant")
    private String code = "SBX_ERROR";

    @Option(names = "--handler", description = "Generate global exception handler")
    private boolean handler;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String exPkg = basePackage + ".error";
        String exClass = normalize(name, "Exception");

        Path exPath = resolveJavaPath(exPkg, exClass);
        String exContent = TemplateEngine.exception(exPkg, exClass, code);
        write(exPath, exContent, force, dryRun);

        if (handler) {
            String handlerPkg = basePackage + ".error.handler";
            Path handlerPath = resolveJavaPath(handlerPkg, "GlobalExceptionHandler");
            String handlerContent = TemplateEngine.exceptionHandler(handlerPkg, exPkg, exClass, status);
            write(handlerPath, handlerContent, force, dryRun);
        }
    }
}
