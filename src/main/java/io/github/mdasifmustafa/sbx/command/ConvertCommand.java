package io.github.mdasifmustafa.sbx.command;

import java.nio.file.Path;
import java.util.Scanner;

import io.github.mdasifmustafa.sbx.build.BuildConverterService;
import io.github.mdasifmustafa.sbx.error.UserAbortException;
import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.io.SbxConfigWriter;
import io.github.mdasifmustafa.sbx.ux.Console;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "convert",
        description = "Convert build tool (maven ↔ gradle)"
)
public class ConvertCommand implements Runnable {

    @Option(
            names = "--to",
            required = true,
            description = "Target build tool (maven | gradle)"
    )
    private String target;

    @Option(
            names = "--kotlin",
            description = "Use Kotlin DSL when converting to Gradle"
    )
    private boolean kotlinDsl;

    @Option(
            names = "--no-sync",
            description = "Do not sync dependencies after conversion. only default build file created"
    )
    private boolean noSync;

    private static final Scanner STDIN = new Scanner(System.in);

    @Override
    public void run() {

        Path root = Path.of(".");
        Path configPath = Path.of("sbx.json");

        var config = SbxConfigReader.read(configPath);

        String current = config.getRuntime().getBuild();

        if (current != null && current.equalsIgnoreCase(target)) {
            Console.info("Project already uses " + target);
            return;
        }

        confirmConversion(target);

        String dsl = kotlinDsl ? "kotlin" : "groovy";

        Console.info("Converting build tool → " + target);

        BuildConverterService.convert(
                root,
                config,
                target,
                dsl,
                true,       // always generate wrapper
                !noSync
        );

        // update sbx.json
        config.getRuntime().setBuild(target.toLowerCase());
        SbxConfigWriter.write(configPath, config);

        Console.success("Build tool converted to " + target);
    }

    private void confirmConversion(String target) {
        String token = "convert-to-" + target;
        System.out.print("Type '" + token + "' to confirm: ");
        String input = STDIN.nextLine();

        if (!token.equals(input)) {
            throw new UserAbortException("Conversion aborted");
        }
    }
}