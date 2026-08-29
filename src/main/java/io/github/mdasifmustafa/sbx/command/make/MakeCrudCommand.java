package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import io.github.mdasifmustafa.sbx.ux.SbxResponse;
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

    @Option(names = "--package", description = "Relative package path (e.g. blog.posts or blog/posts)")
    private String customPackage;

    @Override
    public void run() {
        if (!ensureProject()) {
            return;
        }

        if (!isSimpleName(name)) {
            SbxResponse.error("CRUD name must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages.");
            return;
        }

        if (!validateOptions()) {
            return;
        }

        if (!generateCrudStack()) {
            return;
        }

        if (!dryRun && !validateGeneratedFiles()) {
            SbxResponse.error("CRUD generation stopped: one or more generated files are invalid.");
        }
    }

    private boolean validateOptions() {
        if (record && lombok) {
            SbxResponse.error("CRUD generation failed: --record and --lombok cannot be used together.");
            return false;
        }

        return true;
    }

    private boolean generateCrudStack() {
        String[] packageArgs = customPackage == null || customPackage.isBlank()
                ? new String[0]
                : new String[]{"--package", customPackage};

        String[] entityArgs = concat(
                new String[]{name},
                packageArgs,
                lombok ? new String[]{"--lombok"} : new String[0]
        );
        if (!execute(new MakeEntityCommand(), "entity", entityArgs)) {
            return false;
        }

        String[] requestDtoArgs = concat(
                new String[]{name},
                packageArgs,
                new String[]{"--from-entity", "--request"},
                record ? new String[]{"--record"} : new String[0],
                lombok ? new String[]{"--lombok"} : new String[0]
        );
        if (!execute(new MakeDtoCommand(), "request DTO", requestDtoArgs)) {
            return false;
        }

        String[] responseDtoArgs = concat(
                new String[]{name},
                packageArgs,
                new String[]{"--from-entity", "--response"},
                record ? new String[]{"--record"} : new String[0],
                lombok ? new String[]{"--lombok"} : new String[0]
        );
        if (!execute(new MakeDtoCommand(), "response DTO", responseDtoArgs)) {
            return false;
        }

        String[] mapperArgs = concat(new String[]{name}, packageArgs);
        if (!execute(new MakeMapperCommand(), "mapper", mapperArgs)) {
            return false;
        }

        /*
         * The CRUD controller generator currently creates the repository
         * and service layers as part of its --crud generation.
         */
        String[] controllerArgs = concat(new String[]{name}, packageArgs, new String[]{"--crud"});
        if (!execute(new MakeControllerCommand(), "controller", controllerArgs)) {
            return false;
        }

        if (graphql) {
            String[] graphqlArgs = concat(new String[]{name}, packageArgs);
            if (!execute(new MakeGraphqlCommand(), "GraphQL resolver", graphqlArgs)) {
                return false;
            }
        }

        return true;
    }

    private String[] concat(String[]... groups) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        for (String[] group : groups) {
            if (group == null) {
                continue;
            }
            for (String item : group) {
                if (item != null && !item.isBlank()) {
                    values.add(item);
                }
            }
        }
        return values.toArray(String[]::new);
    }

    private boolean execute(Object command, String operation, String... args) {
        CommandLine commandLine = new CommandLine(command);

        try {
            int exitCode = commandLine.execute(buildArguments(args));
            if (exitCode == 0) {
                return true;
            }

            SbxResponse.error("CRUD generation failed while generating " + operation + ".");
            return false;
        } catch (RuntimeException e) {
            SbxResponse.error(
                    "CRUD generation failed while generating " + operation + ": " + e.getMessage()
            );
            return false;
        }
    }

    private String[] buildArguments(String... args) {
        java.util.ArrayList<String> arguments = new java.util.ArrayList<>();
        for (String arg : args) {
            if (arg != null && !arg.isBlank()) {
                arguments.add(arg);
            }
        }

        if (force) {
            arguments.add("--force");
        }

        if (dryRun) {
            arguments.add("--dry-run");
        }

        return arguments.toArray(String[]::new);
    }

    private boolean validateGeneratedFiles() {
        String basePackage = resolveBasePackage();
        String rootPackage = customPackage == null || customPackage.isBlank()
                ? basePackage
                : resolveRelativePackage(basePackage, customPackage);

        String className = toTitleCase(name);
        String nestedEntityPackage = normalizePackage(
                customPackage == null || customPackage.isBlank()
                        ? name
                        : customPackage + "/" + name
        );
        String entityPackage = rootPackage + ".domain." + nestedEntityPackage;

        String entityPath = Path.of(
                "src",
                "main",
                "java",
                entityPackage.replace('.', '/')
        ).resolve(className + ".java").toString();

        String repositoryPath = Path.of(
                "src",
                "main",
                "java",
                entityPackage.replace('.', '/')
        ).resolve(className + "Repository.java").toString();

        Map<Path, FileExpectation> requiredFiles = new LinkedHashMap<>();

        requiredFiles.put(
                Path.of(entityPath),
                new FileExpectation(entityPackage, className)
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "api",
                        "dto",
                        className + "RequestDto.java"
                ),
                new FileExpectation(rootPackage + ".api.dto", className + "RequestDto")
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "api",
                        "dto",
                        className + "ResponseDto.java"
                ),
                new FileExpectation(rootPackage + ".api.dto", className + "ResponseDto")
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "api",
                        "mapper",
                        className + "Mapper.java"
                ),
                new FileExpectation(rootPackage + ".api.mapper", className + "Mapper")
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "controller",
                        className + "Controller.java"
                ),
                new FileExpectation(rootPackage + ".controller", className + "Controller")
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "service",
                        className + "Service.java"
                ),
                new FileExpectation(rootPackage + ".service", className + "Service")
        );

        requiredFiles.put(
                Path.of(
                        "src",
                        "main",
                        "java",
                        rootPackage.replace('.', '/'),
                        "service",
                        className + "ServiceImpl.java"
                ),
                new FileExpectation(rootPackage + ".service", className + "ServiceImpl")
        );

        requiredFiles.put(
                Path.of(repositoryPath),
                new FileExpectation(entityPackage, className + "Repository")
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
            SbxResponse.error("Missing generated file: " + file);
            return false;
        }

        try {
            String content = Files.readString(file);

            boolean validPackage =
                    content.contains("package " + expectation.packageName() + ";");

            boolean validDeclaration =
                    content.contains(expectation.typeName());

            if (!validPackage || !validDeclaration) {
                SbxResponse.error(
                        "Invalid generated file: " + file
                                + " (package or type declaration mismatch)"
                );
                return false;
            }

            return true;
        } catch (Exception e) {
            SbxResponse.error(
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