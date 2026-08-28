package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "crud",
        description = "Create a full CRUD stack (entity, repository, service, DTO, mapper, controller)"
)
public class MakeCrudCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Base name (e.g. User)")
    private String name;

    @Option(names = "--graphql", description = "Also generate a GraphQL resolver")
    private boolean graphql;

    @Option(names = "--lombok", description = "Use Lombok in generated classes")
    private boolean lombok;

    @Option(names = "--record", description = "Generate DTOs as Java records")
    private boolean record;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) {
            return;
        }

        if (!validateOptions()) {
            return;
        }

        if (!generateCrudStack()) {
            return;
        }

        if (!dryRun && !validateGeneratedFiles()) {
            System.err.println(
                    "CRUD generation stopped: one or more generated files are invalid."
            );
        }
    }

    private boolean validateOptions() {
        if (record && lombok) {
            System.err.println(
                    "CRUD generation failed: --record and --lombok cannot be used together."
            );
            return false;
        }

        return true;
    }

    private boolean generateCrudStack() {
        if (!execute(new MakeEntityCommand(), "entity", name, lombok ? "--lombok" : null)) {
            return false;
        }

        if (!execute(
                new MakeDtoCommand(),
                "request DTO",
                name,
                "--from-entity",
                "--request",
                record ? "--record" : null,
                lombok ? "--lombok" : null
        )) {
            return false;
        }

        if (!execute(
                new MakeDtoCommand(),
                "response DTO",
                name,
                "--from-entity",
                "--response",
                record ? "--record" : null,
                lombok ? "--lombok" : null
        )) {
            return false;
        }

        if (!execute(new MakeMapperCommand(), "mapper", name)) {
            return false;
        }

        /*
         * The CRUD controller generator currently creates the repository
         * and service layers as part of its --crud generation.
         */
        if (!execute(
                new MakeControllerCommand(),
                "controller",
                name,
                "--crud"
        )) {
            return false;
        }

        if (graphql && !execute(
                new MakeGraphqlCommand(),
                "GraphQL resolver",
                name
        )) {
            return false;
        }

        return true;
    }

    private boolean execute(Object command, String operation, String... args) {
        CommandLine commandLine = new CommandLine(command);

        int exitCode = commandLine.execute(buildArguments(args));

        if (exitCode == 0) {
            return true;
        }

        System.err.printf(
                "CRUD generation failed while generating %s.%n",
                operation
        );

        return false;
    }

    private String[] buildArguments(String... args) {
        return java.util.stream.Stream.of(args)
                .filter(java.util.Objects::nonNull)
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(
                                        java.util.ArrayList::new
                                ),
                                arguments -> {
                                    if (force) {
                                        arguments.add("--force");
                                    }

                                    if (dryRun) {
                                        arguments.add("--dry-run");
                                    }

                                    return arguments.toArray(String[]::new);
                                }
                        )
                );
    }

    private boolean validateGeneratedFiles() {
        String basePackage = resolveBasePackage();
        String entityName = name.toLowerCase(Locale.ROOT);

        Path javaRoot = Path.of(
                "src",
                "main",
                "java",
                basePackage.replace('.', '/')
        );

        Map<Path, FileExpectation> requiredFiles = new LinkedHashMap<>();

        requiredFiles.put(
                javaRoot.resolve("domain/" + entityName + "/" + name + ".java"),
                new FileExpectation(
                        basePackage + ".domain." + entityName,
                        name
                )
        );

        requiredFiles.put(
                javaRoot.resolve("api/dto/" + name + "RequestDto.java"),
                new FileExpectation(
                        basePackage + ".api.dto",
                        name + "RequestDto"
                )
        );

        requiredFiles.put(
                javaRoot.resolve("api/dto/" + name + "ResponseDto.java"),
                new FileExpectation(
                        basePackage + ".api.dto",
                        name + "ResponseDto"
                )
        );

        requiredFiles.put(
                javaRoot.resolve("api/mapper/" + name + "Mapper.java"),
                new FileExpectation(
                        basePackage + ".api.mapper",
                        name + "Mapper"
                )
        );

        requiredFiles.put(
                javaRoot.resolve("controller/" + name + "Controller.java"),
                new FileExpectation(
                        basePackage + ".controller",
                        name + "Controller"
                )
        );

        requiredFiles.put(
                javaRoot.resolve("service/" + name + "Service.java"),
                new FileExpectation(
                        basePackage + ".service",
                        name + "Service"
                )
        );

        requiredFiles.put(
                javaRoot.resolve("service/" + name + "ServiceImpl.java"),
                new FileExpectation(
                        basePackage + ".service",
                        name + "ServiceImpl"
                )
        );

        requiredFiles.put(
                javaRoot.resolve(
                        "domain/" + entityName + "/" + name + "Repository.java"
                ),
                new FileExpectation(
                        basePackage + ".domain." + entityName,
                        name + "Repository"
                )
        );

        boolean valid = true;

        for (Map.Entry<Path, FileExpectation> entry : requiredFiles.entrySet()) {
            if (!validateFile(entry.getKey(), entry.getValue())) {
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateFile(Path file, FileExpectation expectation) {
        if (!Files.exists(file)) {
            System.err.println("Missing generated file: " + file);
            return false;
        }

        try {
            String content = Files.readString(file);

            boolean validPackage =
                    content.contains("package " + expectation.packageName() + ";");

            boolean validDeclaration =
                    content.contains(expectation.typeName());

            if (!validPackage || !validDeclaration) {
                System.err.println(
                        "Invalid generated file: " + file
                                + " (package or type declaration mismatch)"
                );
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println(
                    "Unable to read generated file: " + file
                            + " (" + e.getMessage() + ")"
            );
            return false;
        }
    }

    private record FileExpectation(
            String packageName,
            String typeName
    ) {
    }
}