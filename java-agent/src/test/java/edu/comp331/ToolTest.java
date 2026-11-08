package edu.comp331;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToolTest {

    private final Path sandbox = Main.SANDBOX_BASE_DIR;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final ByteArrayOutputStream error = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private PrintStream originalErr;
    private Path fixture;
    private Path extraPath;

    @BeforeEach
    void setUp() throws IOException {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(error, true, StandardCharsets.UTF_8));
        fixture = Files.createTempFile(sandbox, "tool-test-", ".txt");
        Files.writeString(fixture, "fixture content");
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        Files.deleteIfExists(fixture);
        if (extraPath != null) {
            Files.deleteIfExists(extraPath);
        }
    }

    @Test
    void getFileContentReadsExistingFile() {
        new GetFileContentTool().execute(relativePath(fixture));

        assertTrue(stdout().contains("fixture content"));
        assertTrue(stdout().contains(fixture.getFileName().toString()));
    }

    @Test
    void getFileContentTruncatesContentOverItsLimit() throws IOException {
        String content = "A".repeat(GetFileContentTool.MAX_CHARACTERS + 1);
        Files.writeString(fixture, content);

        new GetFileContentTool().execute(relativePath(fixture));

        assertTrue(stdout().contains("A".repeat(GetFileContentTool.MAX_CHARACTERS) + "..."));
        assertTrue(!stdout().contains(content));
    }

    @Test
    void getFileContentRejectsMissingFilesDirectoriesTraversalAndInvalidArguments() {
        GetFileContentTool tool = new GetFileContentTool();

        assertThrows(IllegalArgumentException.class, () -> tool.execute("missing-file.txt"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(""));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("../java-agent/pom.xml"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute());
        assertThrows(IllegalArgumentException.class, () -> tool.execute((String[]) null));
    }

    @Test
    void getFilesInfoListsSandboxForMissingAndNullArguments() {
        GetFilesInfoTool tool = new GetFilesInfoTool();

        tool.execute();
        assertTrue(stdout().contains(fixture.getFileName().toString()));

        output.reset();
        tool.execute((String[]) null);
        assertTrue(stdout().contains("Directory"));
    }

    @Test
    void getFilesInfoListsValidDirectoryAndRejectsInvalidInputs() {
        GetFilesInfoTool tool = new GetFilesInfoTool();

        tool.execute(".");
        assertTrue(stdout().contains(fixture.getFileName().toString()));

        assertThrows(IllegalArgumentException.class, () -> tool.execute(relativePath(fixture)));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("missing-directory"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("../java-agent"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("one", "two"));
    }

    @Test
    void writeFileCreatesAndReplacesFiles() throws IOException {
        WriteFileTool tool = new WriteFileTool();
        extraPath = Files.createTempFile(sandbox, "write-test-", ".txt");
        Files.delete(extraPath);

        tool.execute(relativePath(extraPath), "new content");
        assertEquals("new content", Files.readString(extraPath));
        assertTrue(stdout().contains("Successfully wrote data"));

        output.reset();
        tool.execute(relativePath(extraPath), "replacement content");
        assertEquals("replacement content", Files.readString(extraPath));
        assertTrue(stdout().contains("Successfully wrote data"));
    }

    @Test
    void writeFileAcceptsItsConfiguredMaximum() throws IOException {
        WriteFileTool tool = new WriteFileTool();
        extraPath = Files.createTempFile(sandbox, "write-limit-", ".txt");
        Files.delete(extraPath);
        String content = "B".repeat(WriteFileTool.MAX_CHARACTERS);

        tool.execute(relativePath(extraPath), content);

        assertEquals(content, Files.readString(extraPath));
    }

    @Test
    void writeFileRejectsOverLimitDirectoriesMissingParentsTraversalAndInvalidArguments() {
        WriteFileTool tool = new WriteFileTool();
        String missingParentPath = "missing-parent/file.txt";
        String overLimit = "C".repeat(WriteFileTool.MAX_CHARACTERS + 1);

        assertThrows(IllegalArgumentException.class, () -> tool.execute("", "content"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(missingParentPath, "content"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("../java-agent/outside.txt", "content"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute("one"));
        assertThrows(IllegalArgumentException.class, () -> tool.execute((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(relativePath(fixture), null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(relativePath(fixture), overLimit));
        assertTrue(stderr().contains("Failed to write file"));
    }

    private String relativePath(Path path) {
        return sandbox.relativize(path).toString();
    }

    private String stdout() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return error.toString(StandardCharsets.UTF_8);
    }
}
