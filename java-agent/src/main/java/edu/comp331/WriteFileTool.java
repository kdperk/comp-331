package edu.comp331;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteFileTool implements AgentTool {

    static final int MAX_CHARACTERS = 10100;

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

            Path path = AgentTool.validatePath(userPath);
            if (Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must not be a directory.");
            }
            Path parent = path.getParent();
            if (parent == null || !Files.isDirectory(parent)) {
                throw new IllegalArgumentException("Parent directory must already exist.");
            }

            Files.writeString(path, content);
            System.out.println("\tSuccessfully wrote data to " + path);
        } catch (IOException | RuntimeException e) {
            System.err.println("\tFailed to write file: " + e.getMessage());
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Error writing file: " + e.getMessage(), e);
        }
    }
}
