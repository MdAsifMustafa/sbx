package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.build.DependencyApplyService;
import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.dependency.DependencyRuleRegistry;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.io.SbxConfigWriter;
import io.github.mdasifmustafa.sbx.ux.Console;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Command(
        name = "remove",
        description = "Remove a dependency"
)
public class DependencyRemoveCommand implements Runnable {

    @Parameters(index = "0",
            description = "Dependency (groupId:artifactId or search term)")
    String input;

    @Option(
            names = "--no-apply",
            description = "do not remove dependency from build file"
    )
    boolean noApply;

    @Option(
            names = "--confirm",
            description = "Skip confirmation prompts"
    )
    boolean confirm;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {

        Path root = Path.of(".");
        Path sbxFile = root.resolve("sbx.json");

        SbxConfig config = SbxConfigReader.read(sbxFile);

        Map<String, DependencyConfig> deps =
                config.getDependencies() != null
                        ? new LinkedHashMap<>(config.getDependencies())
                        : new LinkedHashMap<>();

        if (deps.isEmpty()) {
            Console.warning("No dependencies found in sbx.json");
            return;
        }

        String selected = resolveSelection(deps);

        if (selected == null) {
            Console.warning("Dependency removal cancelled.");
            return;
        }

        if (!confirm && !confirmSelection(selected)) {
            Console.warning("Cancelled.");
            return;
        }

        deps.remove(selected);

        setField(config, "dependencies", deps);
        SbxConfigWriter.write(sbxFile, config);

        Console.success("Removed from sbx.json: " + selected);

        if (!noApply) {
            applyRemoval(root, selected);
        }
    }

    // ======================================================
    // SELECTION
    // ======================================================

    private String resolveSelection(Map<String, DependencyConfig> deps) {

        if (deps.containsKey(input)) {
            return input;
        }

        List<String> matches =
                deps.keySet()
                        .stream()
                        .filter(d -> d.toLowerCase()
                                .contains(input.toLowerCase()))
                        .collect(Collectors.toList());

        if (matches.isEmpty()) {
            Console.warning("No matching dependency found: " + input);
            return null;
        }

        if (matches.size() == 1) {
            return matches.get(0);
        }

        return selectFromList(matches);
    }

    private String selectFromList(List<String> list) {

        System.out.println("Select dependency to remove:");

        for (int i = 0; i < list.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, list.get(i));
        }

        System.out.print("Select [1]: ");
        String in = scanner.nextLine().trim();

        int idx = in.isEmpty() ? 0 : Integer.parseInt(in) - 1;

        if (idx < 0 || idx >= list.size()) {
            return null;
        }

        return list.get(idx);
    }

    // ======================================================
    // CONFIRMATION
    // ======================================================

    private boolean confirmSelection(String coords) {

        System.out.println();
        System.out.println("Remove dependency:");
        System.out.println(coords);
        System.out.println();

        System.out.print("(y)es / (n)o / (q)uit: ");

        String in = scanner.nextLine().trim().toLowerCase();

        return in.equals("y") || in.equals("yes");
    }

    // ======================================================
    // APPLY
    // ======================================================

    private void applyRemoval(Path root, String coords) {

        DependencyApplyService.remove(root, coords);

        String[] parts = coords.split(":");

        if (parts.length < 2) return;

        String groupId = parts[0];
        String artifactId = parts[1];

        String processor =
                DependencyRuleRegistry.getProcessorArtifact(artifactId);

        if (processor != null) {

            String processorCoords =
                    groupId + ":" + processor;

            DependencyApplyService.remove(root, processorCoords);
        }

        Console.success("Removed from build file");
    }

    // ======================================================

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}