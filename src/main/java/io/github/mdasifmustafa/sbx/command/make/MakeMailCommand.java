package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "mail",
        description = "Create mail service and optional HTML template"
)
public class MakeMailCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Mail service name (e.g. Notification)")
    private String name;

    @Option(names = "--template", description = "Generate HTML template file")
    private boolean template;

    @Option(names = "--async", description = "Generate async send method stub")
    private boolean async;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".mail";
        String className = normalize(name, "MailService");

        Path servicePath = resolveJavaPath(pkg, className);
        String serviceContent = TemplateEngine.mailService(pkg, className, async);
        write(servicePath, serviceContent, force, dryRun);

        if (template) {
            String templateName = toKebabCase(name) + ".html";
            Path templatePath = Path.of("src/main/resources/templates/mail", templateName);
            String templateContent = TemplateEngine.mailTemplate(name);
            write(templatePath, templateContent, force, dryRun);
        }
    }

    private String toKebabCase(String raw) {
        return raw
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("[^a-zA-Z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                .toLowerCase();
    }
}
