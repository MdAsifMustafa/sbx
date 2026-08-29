package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.runtime.BuildExecutor;
import io.github.mdasifmustafa.sbx.ux.SbxResponse;
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
            SbxResponse.error("❌ " + e.getMessage());
        }
    }
}