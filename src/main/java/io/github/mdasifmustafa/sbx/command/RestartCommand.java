package io.github.mdasifmustafa.sbx.command;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "restart",
    description = "Restart the application"
)
public class RestartCommand implements Runnable {

    @Option(
        names = "--graceful",
        description = "Gracefully restart (SIGTERM + wait)"
    )
    private boolean graceful;

    @Override
    public void run() {
        System.out.println("🔄 Restarting application" + (graceful ? " gracefully..." : "..."));

        try {
            long pid = BuildExecutor.readPid();
            Optional<ProcessHandle> handleOpt = ProcessHandle.of(pid);

            if (handleOpt.isPresent() && handleOpt.get().isAlive()) {
                ProcessHandle handle = handleOpt.get();
                handle.destroy(); // SIGTERM

                if (graceful) {
                	boolean exited = handle.onExit()
                	        .toCompletableFuture()
                	        .completeOnTimeout(null, 15, TimeUnit.SECONDS)
                	        .join() != null;

                    if (!exited) {
                        System.out.println("⚠️ Graceful shutdown timed out. Forcing kill.");
                        handle.destroyForcibly();
                    }
                }

                System.out.println("🛑 Application stopped (PID: " + pid + ")");
            }

            BuildExecutor.clearPid();

        } catch (Exception ignored) {
            // Not running → still valid restart
        }

        new StartCommand().run();
    }
}