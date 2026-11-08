package edu.comp331;

import java.nio.file.Path;
import java.nio.file.Paths;

import static edu.comp331.Main.SANDBOX_BASE_DIR;

public interface AgentTool {

    /**
     * Validates that the given path is within the sandbox directory and returns the
     * normalized path.
     * 
     * @param path
     * @return normalized path within the sandbox directory
     * @throws IllegalArgumentException
     */
    public static Path validatePath(String path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null.");
        }
        Path targetPath = SANDBOX_BASE_DIR.resolve(path).normalize();
        // System.out.println(SANDBOX_BASE_DIR.toString());
        // System.out.println(targetPath.toString());
        if (!targetPath.startsWith(SANDBOX_BASE_DIR)) {
            throw new IllegalArgumentException("Path must be within the sandbox directory.");
        }
        return targetPath;
    }

    public String name();

    public String description();

    public void execute(String... args);
}
