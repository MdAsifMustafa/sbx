package io.github.mdasifmustafa.sbx.command.make;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "crud",
        description = "Create full CRUD stack (entity, repository, service, dto, controller)"
)
public class MakeCrudCommand extends AbstractMakeCommand {

    @Parameters(index = "0", description = "Base name (e.g. User)")
    private String name;

    @Option(names = "--graphql", description = "Also generate GraphQL resolver")
    private boolean graphql;
    
    @Option(names = "--lombok", description = "Use Lombok in DTOs")
    private boolean lombok;

    @Option(names = "--record", description = "Generate DTOs as Java records")
    private boolean record;

    @Option(names = "--force", description = "Overwrite existing files")
    private boolean force;

    @Option(names = "--dry-run", description = "Show output without writing files")
    private boolean dryRun;

    @Override
    public void run() {
        if (!ensureProject()) return;

        // 1. entity
        execute(new MakeEntityCommand(), name, lombok ? "--lombok" : null);

        // 2. repository
        //execute(new MakeRepositoryCommand(), name);

        // 3. service (interface + impl)
        //execute(new MakeServiceCommand(), name);

        // 4. DTOs
        execute(new MakeDtoCommand(),
                name,
                "--from-entity",
                "--request",
                record ? "--record" : null,
                lombok ? "--lombok" : null
        );

        execute(new MakeDtoCommand(),
                name,
                "--from-entity",
                "--response",
                record ? "--record" : null,
                lombok ? "--lombok" : null
        );
        
     // 4️⃣ Mapper
		
		  execute(new MakeMapperCommand(), name );
		 

        // 5. REST controller
        execute(new MakeControllerCommand(), name, "--crud");

        // 6. GraphQL (optional)
        if (graphql) {
            execute(new MakeGraphqlCommand(), name);
        }
        
        
    }

    private void execute(Object command, String... args) {
        picocli.CommandLine cmd = new picocli.CommandLine(command);
        cmd.execute(mergeArgs(args));
    }

    private String[] mergeArgs(String... args) {
        return java.util.Arrays.stream(args)
                .filter(a -> a != null)
                .map(a -> a)
                .toList()
                .stream()
                .flatMap(a -> java.util.stream.Stream.of(a))
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> {
                            if (force) list.add("--force");
                            if (dryRun) list.add("--dry-run");
                            return list.toArray(String[]::new);
                        }
                ));
    }
}