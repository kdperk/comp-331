package edu.comp331;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A tool for getting information about files in the sandbox directory.
 * Will be called by command line arguments.
 */
public class GetFilesInfoTool implements AgentTool {

    @Override
    public String name() {
        return "GetFilesInfo";
    }

    @Override
    public String description() {
        return "Get information about files and folders in the specified directory. Must be within the sandbox directory.";
    }

    /**
     * Execute the tool with the specified path (must be within the sandbox
     * directory). Prints file and folder information to the console.
     * 
     * @param userPath The path to the directory to get information about.
     */
    public void execute(String... args) {
        // default to the base sandbox directory if no path is provided
        String userPath = (args == null || args.length == 0) ? "" : args[0];
        if (args != null && args.length > 1) {
            throw new IllegalArgumentException("Expected at most one path argument.");
        }

        Path path = AgentTool.validatePath(userPath);

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory.");
        }

        List<Path> files = new ArrayList<>(), directories = new ArrayList<>();

        System.out.println("\n\tDirectory " + path.toString() + "\n");
        System.out.println("Type" + " ".repeat(5) + "Size (bytes)" + " ".repeat(5) + "Name");
        System.out.println("-".repeat(4) + " ".repeat(5) + "-".repeat(12) + " ".repeat(5) + "-".repeat(4));

        try (Stream<Path> paths = Files.list((path))) {
            paths.forEach((val) -> {
                if (Files.isRegularFile(val)) {
                    long size = 0;
                    try {
                        size = Files.size(val);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    files.add(val);
                    System.out.printf("%-4s" + " ".repeat(5), "F");
                    System.out.printf("%-12d" + " ".repeat(5), size);
                    System.out.printf("%-50s\n", path.relativize(val));
                } else if (Files.isDirectory(val)) {
                    directories.add(val);
                    System.out.printf("%-4s" + " ".repeat(5), "D");
                    System.out.printf("%-12s" + " ".repeat(5), "");
                    System.out.printf("%-50s\n", path.relativize(val));
                }
            });

        } catch (IOException e) {
            System.err.println("Error occurred while getting files info: " + e.getMessage());
        }
    }

}
