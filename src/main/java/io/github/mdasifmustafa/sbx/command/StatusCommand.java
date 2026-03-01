package io.github.mdasifmustafa.sbx.command;

import io.github.mdasifmustafa.sbx.runtime.AppRuntimeInfo;
import io.github.mdasifmustafa.sbx.runtime.RuntimeState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.HttpURLConnection;
import java.net.URI;

@Command(
        name = "status",
        description = "Show application runtime status"
)
public class StatusCommand implements Runnable {

    @Option(
            names = "--health",
            description = "Check application health via Actuator"
    )
    private boolean health;

    @Override
    public void run() {
        try {
            AppRuntimeInfo runtime = RuntimeState.read();

            boolean alive = ProcessHandle.of(runtime.pid())
                    .map(ProcessHandle::isAlive)
                    .orElse(false);

            if (!alive) {
                System.out.println("🔴 Application not running (stale PID)");
                return;
            }

            System.out.println("🟢 Application running (PID: " + runtime.pid() + ")");
            System.out.println("   ↳ http://" + runtime.host() + ":" + runtime.port());

            if (health) {
                checkHealth(runtime);
            }

        } catch (Exception e) {
            System.out.println("🔴 Application is not running");
        }
    }

    private void checkHealth(AppRuntimeInfo runtime) {
        try {
            URI uri = URI.create(
                    "http://" + runtime.host() + ":" +
                    runtime.port() + runtime.actuatorPath()
            );

            HttpURLConnection con =
                    (HttpURLConnection) uri.toURL().openConnection();

            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);

            int code = con.getResponseCode();

            if (code == 200) {
                System.out.println("❤️ Health: UP");
            } else {
                System.out.println("⚠️ Health: DOWN (HTTP " + code + ")");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Health check failed");
        }
    }
}