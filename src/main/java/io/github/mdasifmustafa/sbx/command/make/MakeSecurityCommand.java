package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "security", description = "Create starter security configuration")
public class MakeSecurityCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Security config name (e.g. App)")
    private String name;

    @Option(names = "--jwt", description = "Add JWT integration TODO")
    private boolean jwt;

    @Option(names = "--roles", description = "Add role-based matcher examples")
    private boolean roles;

    @Option(names = "--method-security", description = "Enable @EnableMethodSecurity")
    private boolean methodSecurity;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".security";
        String className = normalize(name, "SecurityConfig");

        Path path = resolveJavaPath(pkg, className);
        String content = TemplateEngine.securityConfig(pkg, className, methodSecurity, jwt, roles);
        write(path, content, force, dryRun);
    }
}
