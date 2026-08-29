package io.github.mdasifmustafa.sbx.command.make;

import java.nio.file.Path;

import io.github.mdasifmustafa.sbx.template.TemplateEngine;
import io.github.mdasifmustafa.sbx.template.Templates;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "controller",
        description = "Create a new Spring controller"
)
public class MakeControllerCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Controller name (supports sub-packages)")
    private String name;

    @Option(names = "--rest", description = "Generate REST controller")
    private boolean rest;

    @Option(names = "--crud", description = "Generate CRUD endpoints")
    private boolean crud;

    @Option(names = "--graphql", description = "Generate GraphQL controller")
    private boolean graphql;

    @Option(names = "--service", description = "Generate service and inject it")
    private boolean service;

    @Option(names = "--model", description = "Attach model to controller")
    private String model;

    @Option(names = "--path", description = "Base request mapping path")
    private String path;

    @Option(names = "--versioned", description = "Prefix path with /api/v1")
    private boolean versioned;

    @Option(names = "--package", description = "Custom package relative to base")
    private String customPackage;

    @Option(names = "--validation", description = "Enable @Validated on controller and generate DTOs with validation")
    private boolean validation;

    @Option(names = "--skip-related", description = "Skip generating DTOs and mapper (used when called from CRUD generator)")
    private boolean skipRelated;
    @Option(names = "--test", description = "Generate test class")
    private boolean test;

    @Option(names = "--dry-run", description = "Show generated output without writing files")
    private boolean dryRun;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Override
    public void run() {
        if (!ensureProject()) return;

        if (!isSimpleName(name)) {
            SbxResponse.error("Controller name must be a simple name like 'PostBlog' or 'post blog'. Use --package for nested packages.");
            return;
        }

        NameParts parts = parseName(name, "Controller");

        String basePackage = resolveBasePackage();
        String controllerRoot = resolvePackage(basePackage, customPackage, "controller");
        String packageName = controllerRoot;

        if (!parts.subPackage().isEmpty()) {
            packageName += "." + parts.subPackage();
        }

        ControllerType type = resolveType();

        String mappingPath = resolvePath(parts.className());

        Path controllerPath = resolveJavaPath(packageName, parts.className());

        String content = TemplateEngine.controller(
            packageName,
            parts.className().replace("Controller", ""),
            type,
            mappingPath,
            validation
        );

        write(controllerPath, content, force, dryRun);

        if (type == ControllerType.CRUD) {
            generateCrudLayer(basePackage, parts);
        } else if (service) {
            generateService(basePackage, parts);
        }

        if (test) {
            generateTest(packageName, parts);
        }
    }

    @Override
    protected String[] requiredDependencies() {
        // Only request dependencies needed by the options the user selected.
        java.util.List<String> req = new java.util.ArrayList<>();
        if (crud) {
            req.add("spring-data-jpa");
        }
        // Controllers and REST endpoints require the web starter
        if (rest || crud || service) {
            req.add("web");
        }
        // If DTOs/validation or entities are requested elsewhere, add keys accordingly.
        return req.toArray(new String[0]);
    }

    private void generateCrudLayer(String basePackage, NameParts parts) {

        String entityName = parts.className().replace("Controller", "");
        String rootPackage = resolvePackage(basePackage, customPackage, "");
        String servicePackage = rootPackage + ".service";

        // ---- Interface
        Path serviceInterfacePath =
                resolveJavaPath(servicePackage, entityName + "Service");

        write(
                serviceInterfacePath,
                TemplateEngine.crudServiceInterface(servicePackage, entityName),
                force,
                dryRun
        );

        // ---- Implementation
        Path serviceImplPath =
                resolveJavaPath(servicePackage, entityName + "ServiceImpl");

        write(
                serviceImplPath,
                TemplateEngine.crudServiceImpl(servicePackage, entityName),
                force,
                dryRun
        );

        // =============================
        // REPOSITORY
        // =============================
        String repoPackage = rootPackage + ".domain." + entityName.toLowerCase();

        Path repoPath =
                resolveJavaPath(repoPackage, entityName + "Repository");

        write(
                repoPath,
                TemplateEngine.repository(repoPackage, entityName, false, null),
                force,
                dryRun
        );

        // Optionally generate DTOs and mapper for CRUD flows
        if (!skipRelated) {
            String[] packageArgs = customPackage == null || customPackage.isBlank()
                    ? new String[0]
                    : new String[]{"--package", customPackage};

            String[] requestDtoArgs = concatArgs(new String[]{entityName}, packageArgs, new String[]{"--from-entity", "--request"}, validation ? new String[]{"--validation"} : new String[0]);
            if (!executeNested(new io.github.mdasifmustafa.sbx.command.make.MakeDtoCommand(), "request DTO", requestDtoArgs)) {
                return;
            }

            String[] responseDtoArgs = concatArgs(new String[]{entityName}, packageArgs, new String[]{"--from-entity", "--response"}, validation ? new String[]{"--validation"} : new String[0]);
            if (!executeNested(new io.github.mdasifmustafa.sbx.command.make.MakeDtoCommand(), "response DTO", responseDtoArgs)) {
                return;
            }

            String[] mapperArgs = concatArgs(new String[]{entityName}, packageArgs, new String[]{"--update-method"});
            if (!executeNested(new io.github.mdasifmustafa.sbx.command.make.MakeMapperCommand(), "mapper", mapperArgs)) {
                return;
            }
        }
    }

    private boolean executeNested(Object command, String operation, String... args) {
        picocli.CommandLine commandLine = new picocli.CommandLine(command);

        try {
            int exitCode = commandLine.execute(buildNestedArguments(args));
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

    private String[] buildNestedArguments(String... args) {
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

        // Propagate dependency flags from AbstractMakeCommand accessors
        try {
            if (isNoDepsCheck()) {
                arguments.add("--no-deps-check");
            }
            if (isAutoApplyDeps()) {
                arguments.add("--auto-apply-deps");
            }
            if (isDepsOnly()) {
                arguments.add("--deps-only");
            }
        } catch (Exception ignored) {
            // ignore
        }

        // Propagate validation flag so nested DTO generation uses validation when requested
        if (validation) {
            arguments.add("--validation");
        }

        return arguments.toArray(String[]::new);
    }

    private String[] concatArgs(String[]... groups) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        for (String[] group : groups) {
            if (group == null) continue;
            for (String item : group) {
                if (item != null && !item.isBlank()) {
                    values.add(item);
                }
            }
        }
        return values.toArray(String[]::new);
    }

    private ControllerType resolveType() {
        if (graphql) return ControllerType.GRAPHQL;
        if (crud) return ControllerType.CRUD;
        if (rest) return ControllerType.REST;
        return ControllerType.MVC;
    }

    private String resolvePath(String className) {
        if (path != null) return path;
        String base = className.replace("Controller", "").toLowerCase();
        return "/" + base + "s";
    }

    private void generateService(String basePackage, NameParts parts) {
        String serviceName = parts.className().replace("Controller", "Service");
        String servicePackage = resolvePackage(basePackage, customPackage, "service");

        Path path = resolveJavaPath(servicePackage, serviceName);

        String content = Templates.service(servicePackage, serviceName, model);
        write(path, content, force, dryRun);
    }

    private void generateTest(String packageName, NameParts parts) {
        Path path = Path.of(
                "src/test/java",
                packageName.replace(".", "/"),
                parts.className() + "Test.java"
        );

        String content = Templates.controllerTest(
                packageName,
                parts.className()
        );

        write(path, content, force, dryRun);
    }
}