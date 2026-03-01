package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.command.make.MakeControllerCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeCrudCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeCacheCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeDtoCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeEntityCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeExceptionCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeEventCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeGraphqlCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeMapStructCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeMailCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeMessageCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeMigrationCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeModuleCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeRepositoryCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeSchedulerCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeSecurityCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeServiceCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeSpecCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeTestCommand;
import io.github.mdasifmustafa.sbx.command.make.MakeValidatorCommand;
import picocli.CommandLine.Command;

@Command(
        name = "make",
        description = "Generate application components",
        subcommands = {
                MakeControllerCommand.class,
                MakeCrudCommand.class,
                MakeDtoCommand.class,
                MakeGraphqlCommand.class,
                MakeMigrationCommand.class,
                MakeServiceCommand.class, 
                MakeRepositoryCommand.class,
                MakeEntityCommand.class, 
                MakeEventCommand.class,
                MakeMailCommand.class,
                MakeSchedulerCommand.class,
                MakeExceptionCommand.class,
                MakeValidatorCommand.class,
                MakeSpecCommand.class,
                MakeSecurityCommand.class,
                MakeCacheCommand.class,
                MakeMessageCommand.class,
                MakeMapStructCommand.class,
                MakeTestCommand.class,
                MakeModuleCommand.class
 
        }
)
public class MakeCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Usage: sbx make <type> <name>");
    }
}
