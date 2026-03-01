package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.io.SbxConfigReader;
import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import io.github.mdasifmustafa.sbx.runtime.BuildTool;
import io.github.mdasifmustafa.sbx.runtime.BuildToolResolver;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Command(
        name = "clean",
        description = "Clean build artifacts"
)
public class CleanCommand implements Runnable {

    @Override
    public void run() {
        var config = SbxConfigReader.read(Path.of("sbx.json"));
        BuildTool tool = resolveBuildTool(config.getRuntime().getBuild());

        List<String> command = new ArrayList<>();
        command.add(BuildToolResolver.resolve(tool));

        switch (tool) {
            case MAVEN -> command.add("clean");
            case GRADLE -> command.add("clean");
        }

        BuildExecutor.run(command);
    }

    private BuildTool resolveBuildTool(String value) {
        if ("maven".contains(value)) {
            return BuildTool.MAVEN;
        }
        if ("gradle".contains(value)) {
            return BuildTool.GRADLE;
        }
        throw new IllegalStateException("Unsupported build tool: " + value);
    }
}