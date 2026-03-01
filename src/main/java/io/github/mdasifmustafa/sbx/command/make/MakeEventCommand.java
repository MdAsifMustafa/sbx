package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "event",
        description = "Create domain event and optional listener"
)
public class MakeEventCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Event name (e.g. UserCreated)")
    private String name;

    @Option(names = "--payload", description = "Generate event with payload field")
    private boolean payload;

    @Option(names = "--listener", description = "Generate @EventListener component")
    private boolean listener;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String eventPkg = basePackage + ".event";
        String eventClass = normalize(name, "Event");

        Path eventPath = resolveJavaPath(eventPkg, eventClass);
        String eventContent = TemplateEngine.event(eventPkg, eventClass, payload);
        write(eventPath, eventContent, force, dryRun);

        if (listener) {
            String listenerPkg = basePackage + ".event.listener";
            String listenerClass = eventClass + "Listener";
            Path listenerPath = resolveJavaPath(listenerPkg, listenerClass);
            String listenerContent = TemplateEngine.eventListener(
                    listenerPkg,
                    eventPkg,
                    eventClass,
                    listenerClass);
            write(listenerPath, listenerContent, force, dryRun);
        }
    }
}
