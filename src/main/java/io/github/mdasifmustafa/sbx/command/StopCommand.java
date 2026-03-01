package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import picocli.CommandLine.Command;

@Command(name = "stop", description = "Stop the running application")
public class StopCommand implements Runnable {

    @Override
    public void run() {
        try {
            long pid = BuildExecutor.readPid();
            ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
            BuildExecutor.clearPid();
            System.out.println("🛑 Application stopped (PID: " + pid + ")");
        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }
}