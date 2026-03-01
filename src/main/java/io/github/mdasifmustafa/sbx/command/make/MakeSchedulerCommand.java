package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "scheduler", description = "Create scheduled job class")
public class MakeSchedulerCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Scheduler name (e.g. Cleanup)")
    private String name;

    @Option(names = "--cron", description = "Cron expression")
    private String cron;

    @Option(names = "--fixed-rate", description = "Fixed rate in ms")
    private String fixedRate;

    @Option(names = "--fixed-delay", description = "Fixed delay in ms")
    private String fixedDelay;

    @Option(names = "--async", description = "Generate @Async method")
    private boolean async;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".scheduler";
        String className = normalize(name, "Scheduler");

        String scheduleType = "cron";
        String expr = "0 */5 * * * *";

        if (fixedRate != null && !fixedRate.isBlank()) {
            scheduleType = "fixedRate";
            expr = fixedRate;
        } else if (fixedDelay != null && !fixedDelay.isBlank()) {
            scheduleType = "fixedDelay";
            expr = fixedDelay;
        } else if (cron != null && !cron.isBlank()) {
            expr = cron;
        }

        Path path = resolveJavaPath(pkg, className);
        String content = TemplateEngine.scheduler(pkg, className, expr, scheduleType, async);
        write(path, content, force, dryRun);
    }
}
