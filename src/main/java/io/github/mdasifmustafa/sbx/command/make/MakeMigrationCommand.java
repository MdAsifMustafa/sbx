package io.github.mdasifmustafa.sbx.command.make;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(
        name = "migration",
        description = "Create database migration (Flyway)"
)
public class MakeMigrationCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Migration name (e.g. create_users_table)")
    private String name;

    @Option(names = "--sql", description = "Generate empty SQL migration")
    private boolean sql;

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        int nextVersion = resolveNextVersion();

        String fileName = "V" + nextVersion + "__" + name + ".sql";

        Path path = Path.of(
                "src/main/resources/db/migration",
                fileName
        );

        String content = TemplateEngine.migration(name);

        write(path, content, force, dryRun);
    }

    private int resolveNextVersion() {
        Path dir = Path.of("src/main/resources/db/migration");
        if (!Files.exists(dir)) return 1;

        Pattern pattern = Pattern.compile("V(\\d+)__.*\\.sql");

        try {
            return Files.list(dir)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .map(m -> Integer.parseInt(m.group(1)))
                    .max(Comparator.naturalOrder())
                    .orElse(0) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}