package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "message", description = "Create producer/consumer message scaffolding")
public class MakeMessageCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Message base name (e.g. OrderCreated)")
    private String name;

    @Option(names = "--topic", description = "Topic/queue name")
    private String topic = "app.topic";

    @Option(names = "--group", description = "Consumer group")
    private String group = "app-group";

    @Option(names = "--payload", description = "Generate payload class")
    private boolean payload;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".messaging";
        String classBase = name;

        Path producerPath = resolveJavaPath(pkg, classBase + "Producer");
        String producerContent = TemplateEngine.messageProducer(pkg, classBase, topic);
        write(producerPath, producerContent, force, dryRun);

        Path consumerPath = resolveJavaPath(pkg, classBase + "Consumer");
        String consumerContent = TemplateEngine.messageConsumer(pkg, classBase, topic, group);
        write(consumerPath, consumerContent, force, dryRun);

        if (payload) {
            Path payloadPath = resolveJavaPath(pkg, classBase + "Payload");
            String payloadContent = TemplateEngine.messagePayload(pkg, classBase);
            write(payloadPath, payloadContent, force, dryRun);
        }
    }
}
