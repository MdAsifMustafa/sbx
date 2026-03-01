package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "cache", description = "Create cache service scaffold")
public class MakeCacheCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Cache service name (e.g. Product)")
    private String name;

    @Option(names = "--provider", description = "Cache provider (caffeine|redis)")
    private String provider = "caffeine";

    @Option(names = "--ttl", description = "TTL hint (e.g. 10m)")
    private String ttl = "10m";

    @Option(names = "--key-prefix", description = "Cache name/prefix")
    private String keyPrefix = "app-cache";

    @Option(names = "--force", description = "Overwrite existing file")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        String basePackage = resolveBasePackage();
        String pkg = basePackage + ".cache";
        String className = normalize(name, "CacheService");

        Path path = resolveJavaPath(pkg, className);
        String content = TemplateEngine.cacheService(pkg, className, provider, ttl, keyPrefix);
        write(path, content, force, dryRun);
    }
}
