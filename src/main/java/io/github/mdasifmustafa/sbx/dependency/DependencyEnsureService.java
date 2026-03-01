package io.github.mdasifmustafa.sbx.dependency;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import io.github.mdasifmustafa.sbx.config.SbxConfig;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.ux.Console;

public class DependencyEnsureService {
	private static final Scanner IN = new Scanner(System.in);

    /**
     * Ensures a dependency exists in sbx.json.
     * Optionally applies it to the build.
     */
	public static void ensure(
	        Path projectRoot,
	        String coords,
	        String scope,
	        boolean interactive
	) {

	    Path sbxFile = projectRoot.resolve("sbx.json");
	    SbxConfig config = SbxConfigReader.read(sbxFile);

	    Map<String, DependencyConfig> deps = config.getDependencies();
	    if (deps != null && deps.containsKey(coords)) {
	        return; // already present
	    }

	    Console.warning("Required dependency not found: " + coords);

	    boolean add = interactive
	        ? askYesNo("Do you want to add it?", false)
	        : true;

	    if (!add) {
	        Console.warning("Dependency not added. You may face runtime errors.");
	        return;
	    }

	    Console.info("Adding dependency using 'dependency add' command...");

	    int exitCode =
	            new picocli.CommandLine(
	                    new io.github.mdasifmustafa.sbx.command.DependencyAddCommand()
	            ).execute(
	                    coords,
	                    "--confirm"
	            );

	    if (exitCode != 0) {
	        Console.error("Failed to add dependency: " + coords);
	    }
	}

    // --------------------------------------------------

    private static boolean askYesNo(String q, boolean def) {
        String suffix = def ? " (Y/n): " : " (y/N): ";
        System.out.print(q + suffix);

        String input = IN.nextLine().trim().toLowerCase();
        return input.isEmpty() ? def : input.startsWith("y");
    }
}