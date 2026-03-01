package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.build.DependencyApplyService;
import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.dependency.DependencyRuleRegistry;
import io.github.mdasifmustafa.sbx.dependency.MavenArtifact;
import io.github.mdasifmustafa.sbx.dependency.MavenCentralSearch;
import io.github.mdasifmustafa.sbx.dependency.MavenCentralValidator;
import io.github.mdasifmustafa.sbx.dependency.MavenRankSearch;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.io.SbxConfigWriter;
import io.github.mdasifmustafa.sbx.ux.Console;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add", description = "Add a dependency to sbx.json")
public class DependencyAddCommand implements Runnable {

    @Parameters(index = "0",
            description = "Dependency keyword or groupId:artifactId[:version]")
    String input;

    @Option(names = "--no-apply",
            description = "Do not apply dependency to build file")
    boolean noApply;

    @Option(names = "--confirm", description = "Skip confirmation prompt")
    boolean confirm;

    @Option(names = "--format",
            description = "Format build file after applying dependency")
    boolean format;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {

        MavenArtifact artifact = resolveArtifact(input);

        if (artifact == null) {
            Console.error("Dependency resolution cancelled.");
            return;
        }

        String coords = artifact.coords();

        String[] parts = coords.split(":");

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts.length >= 3 ? parts[2] : null;

        DependencyConfig model =
                buildDependencyModel(groupId, artifactId, version);

        persistDependency(groupId, artifactId, model);

        if (!noApply) {
            applyDependency(coords, model);
        }

        Console.success("Dependency added: " + coords);
    }

    // ==================================================
    // RESOLUTION PIPELINE
    // ==================================================

    private MavenArtifact resolveArtifact(String input) {

        while (true) {

            MavenArtifact artifact;

            if (input.contains(":")) {
                artifact = resolveExact(input);
            } else {
                artifact = searchFlow(input);
            }

            if (artifact == null) return null;

            if (confirm || confirmSelection(artifact)) {
                return artifact;
            }

            String action = readAction();

            switch (action) {
                case "s":
                    input = prompt("Search term: ");
                    break;
                case "e":
                    input = prompt("Enter groupId:artifactId[:version]: ");
                    break;
                case "q":
                    return null;
            }
        }
    }

    private MavenArtifact resolveExact(String coords) {

        String[] p = coords.split(":");

        String g = p[0];
        String a = p[1];
        String v = p.length >= 3 ? p[2] : null;

        MavenArtifact meta = MavenCentralSearch.resolve(g, a);

        // Fallback if search API fails
        if (meta == null) {

            if (!MavenCentralValidator.exists(g, a)) {
                Console.error(
                        "Dependency not found in Maven Central: "
                        + g + ":" + a
                );
                return null;
            }

            // minimal metadata
            meta = new MavenArtifact(g, a, v, "");
        }

        if (v != null) {
            return new MavenArtifact(g, a, v, meta.description);
        }

        return meta;
    }

    private MavenArtifact searchFlow(String keyword) {

        while (true) {

            List<MavenRankSearch.Item> results =
                    MavenRankSearch.search(keyword);

            if (results.isEmpty()) {
                Console.error("No results found.");
                String action = readAction();
                if ("s".equals(action)) {
                    keyword = prompt("Search term: ");
                    continue;
                }
                if ("e".equals(action)) {
                    return resolveExact(
                            prompt("Enter groupId:artifactId[:version]: "));
                }
                return null;
            }

            printResults(results);

            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("s")) {
                keyword = prompt("Search term: ");
                continue;
            }

            if (choice.equalsIgnoreCase("e")) {
                return resolveExact(
                        prompt("Enter groupId:artifactId[:version]: "));
            }

            if (choice.equalsIgnoreCase("q")) {
                return null;
            }

            int idx = Integer.parseInt(choice) - 1;

            if (idx < 0 || idx >= results.size()) continue;

            MavenRankSearch.Item item = results.get(idx);

            MavenArtifact artifact =
                    MavenCentralSearch.resolve(
                            item.groupId,
                            item.artifactId
                    );

            if (artifact != null) {
                return artifact;
            }

            Console.error("Failed to validate artifact.");
        }
    }

    // ==================================================
    // CONFIRMATION
    // ==================================================

    private boolean confirmSelection(MavenArtifact a) {

        System.out.println();
        System.out.println("Selected dependency:");
        System.out.println();
        System.out.println("  " + a.coords());

        if (a.description != null && !a.description.isBlank()) {
            System.out.println("  " + a.description);
        }

        System.out.println();
        System.out.println("--------------------------------");
        System.out.println("y = confirm");
        System.out.println("s = search");
        System.out.println("e = enter");
        System.out.println("q = quit");
        System.out.print("> ");

        String in = scanner.nextLine().trim().toLowerCase();

        return in.isEmpty() || in.equals("y");
    }

    private String readAction() {
        System.out.print("> ");
        return scanner.nextLine().trim().toLowerCase();
    }

    private void printResults(List<MavenRankSearch.Item> results) {

        System.out.println();
        System.out.println("Search results:");
        System.out.println();

        for (int i = 0; i < results.size(); i++) {
            var r = results.get(i);

            System.out.printf("%d) %s:%s%n",
                    i + 1,
                    r.groupId,
                    r.artifactId);

            if (r.description != null) {
                System.out.println("   " + r.description);
            }

            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("[number] select");
        System.out.println("s = search again");
        System.out.println("e = enter manually");
        System.out.println("q = quit");
        System.out.print("> ");
    }

    private String prompt(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }

    // ==================================================
    // MODEL / PERSIST / APPLY (UNCHANGED)
    // ==================================================

    private DependencyConfig buildDependencyModel(
            String groupId,
            String artifactId,
            String version
    ) {

        DependencyConfig dep = new DependencyConfig();

        dep.version = version;

        // default scope
        dep.scope = "implementation";

        // apply external rules
        DependencyRuleRegistry.applyRules(
                groupId,
                artifactId,
                dep
        );

        return dep;
    }

    private void persistDependency(
            String groupId,
            String artifactId,
            DependencyConfig model
    ) {

        Path sbxFile = Path.of("sbx.json");
        SbxConfig config = SbxConfigReader.read(sbxFile);

        Map<String, DependencyConfig> deps =
                config.getDependencies() != null
                        ? new LinkedHashMap<>(config.getDependencies())
                        : new LinkedHashMap<>();

        String key = groupId + ":" + artifactId;
        deps.put(key, model);

        setField(config, "dependencies", deps);
        SbxConfigWriter.write(sbxFile, config);
    }

    private void applyDependency(
            String coords,
            DependencyConfig model
    ) {

        DependencyApplyService.apply(
                Path.of("."),
                coords,
                model,
                format
        );

    }

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass()
                    .getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to update sbx.json", e);
        }
    }
}