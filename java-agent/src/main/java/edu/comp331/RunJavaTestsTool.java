package edu.comp331;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RunJavaTestsTool implements AgentTool {

    static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Override
    public String name() {
        return "RunJavaTests";
    }

    @Override
    public String description() {
        return "Run Maven tests in the sandbox directory.";
    }

    @Override
    public void execute(String... args) {
        if (args == null || args.length != 0) {
            throw new IllegalArgumentException("This tool does not accept arguments.");
        }

        Path sandbox = Main.SANDBOX_BASE_DIR;
        if (!Files.isDirectory(sandbox)) {
            throw new IllegalArgumentException("Sandbox directory does not exist: " + sandbox);
        }

        String mavenCommand = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win") ? "mvn.cmd" : "mvn";

        Process process = null;
        try {
            process = new ProcessBuilder(mavenCommand, "test")
                    .directory(sandbox.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();

            if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                System.err.println("Maven tests failed: timed out after 30 seconds.");
                throw new RuntimeException("Maven tests timed out after 30 seconds.");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.err.println("Maven tests failed with exit code " + exitCode + ".");
                throw new RuntimeException("Maven tests failed with exit code " + exitCode + ".");
            }

            System.out.println("Maven tests completed successfully.");
        } catch (IOException e) {
            System.err.println("Failed to start Maven tests: " + e.getMessage());
            throw new RuntimeException("Failed to start Maven tests.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
            System.err.println("Maven tests were interrupted.");
            throw new RuntimeException("Maven tests were interrupted.", e);
        }
    }
}
