package edu.comp331;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GetFileContentTool implements AgentTool {

    static final int MAX_CHARACTERS = 10000;

    @Override
    public String name() {
        return "GetFileContent";
    }

    @Override
    public String description() {
        return "Get the content of a file in the sandbox directory. Must be within the sandbox directory.";
    }

    @Override
    public void execute(String... args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one path argument.");
        }
        String userPath = args[0];
        Path path = AgentTool.validatePath(userPath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path must be a regular file.");
        }
        try {
            StringBuilder contentBuilder = new StringBuilder();
            try (var reader = Files.newBufferedReader(path)) {
                int c;
                while (contentBuilder.length() < MAX_CHARACTERS && (c = reader.read()) != -1) {
                    contentBuilder.append((char) c);
                }
                if (reader.read() != -1) {
                    System.out.println("\tFile content exceeds " + MAX_CHARACTERS + " characters. Truncating output.");
                    contentBuilder.append("...");
                }
            }
            String content = contentBuilder.toString();

            System.out.println("\n\tContent of file " + path.toString() + "\n");
            System.out.println(content + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }

}
