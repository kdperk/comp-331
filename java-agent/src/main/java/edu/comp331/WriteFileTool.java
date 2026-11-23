package edu.comp331;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static edu.comp331.Main.SANDBOX_BASE_DIR;

public class WriteFileTool implements AgentTool {

    static final int MAX_CHARACTERS = 10100;
    private final boolean confirmationRequired;
    private final BufferedReader input;
    private final PrintStream output;

    public WriteFileTool() {
        this(false);
    }

    public WriteFileTool(boolean confirmationRequired) {
        this(confirmationRequired, System.in, System.out);
    }

    WriteFileTool(boolean confirmationRequired, InputStream input, PrintStream output) {
        this.confirmationRequired = confirmationRequired;
        this.input = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.output = output;
    }

    @Override
    public String name() {
        return "WriteFile";
    }

    @Override
    public String description() {
        return "Write text to a file in the sandbox directory. Must be within the sandbox directory.";
    }

    @Override
    public void execute(String... args) {
        try {
            if (args == null || args.length != 2) {
                throw new IllegalArgumentException("Expected a file path and text value.");
            }

            String userPath = args[0];
            String content = args[1];
            if (content == null) {
                throw new IllegalArgumentException("Text value must not be null.");
            }
            if (content.length() > MAX_CHARACTERS) {
                throw new IllegalArgumentException("Text value must not exceed 10100 characters.");
            }

            Path path = SANDBOX_BASE_DIR.resolve(userPath).normalize();
            if (Files.exists(path)) {
                path = AgentTool.validatePath(userPath);
            } else if (path.getParent() != null) {
                AgentTool.validatePath(path.getParent().toString());
            }

            if (Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must not be a directory.");
            }

            if (confirmationRequired && !confirmWrite(path)) {
                output.println("\tWrite cancelled.");
                return;
            }

            Files.writeString(path, content);
            output.println("\tSuccessfully wrote data to " + path);
        } catch (IOException | RuntimeException e) {
            System.err.println("\tFailed to write file: " + e.getMessage());
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Error writing file: " + e.getMessage(), e);
        }
    }

    private boolean confirmWrite(Path path) {
        output.printf("\tWrite to %s? [y/N]: ", path);
        output.flush();
        try {
            String response = input.readLine();
            return response != null
                    && (response.trim().equalsIgnoreCase("y")
                            || response.trim().equalsIgnoreCase("yes"));
        } catch (IOException e) {
            output.println("\tCould not read confirmation; write cancelled.");
            return false;
        }
    }
}
