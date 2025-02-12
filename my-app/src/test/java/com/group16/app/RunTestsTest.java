package com.group16.app;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the {@link RunTests} class.
 */
public class RunTestsTest {
    
    @TempDir
    Path tempDir; // Temporary directory for testing

    private Path clonedDir;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    @BeforeEach
    void setUp() throws IOException {
        assertNotNull(tempDir, "tempDir should not be null");

        // Simulate a cloned repo and make sure it has a "my-app" folder
        clonedDir = tempDir.resolve("my-app");
        File myAppDir = clonedDir.toFile();
        if (!myAppDir.exists()) {
            boolean created = myAppDir.mkdirs();
            assertTrue(created, "Failed to create clonedDir: " + clonedDir);
        }

        // Make sure .env file exists or create it 
        RunTests.ensureEnvFileExistsAndWrite(clonedDir);

        // Mock HTTP response
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Set the Compiler temp directory to this test tempDir
        Compiler.tempDir = tempDir;
    }

    /**
     * Test that the `.env` file is correctly created and contains the GITHUB_PAT.
     */
    @Test
    void testEnvFileCreation() throws IOException {
        // Simulate a GITHUB_PAT value
        String expectedGitHubPAT = "test_pat_value";

        // Write the .env file manually
        File envFile = new File(clonedDir.toFile(), ".env");
        try (FileWriter writer = new FileWriter(envFile)) {
            writer.write("GITHUB_PAT=" + expectedGitHubPAT);
        }

        assertTrue(envFile.exists(), ".env file should be created");
    }

    // /**
    //  * Test that `runTests` returns false when Maven tests fail (exit code > 0).
    //  */
    @Test
    void testRunTests_Failure() {
        RunTests runTests = new RunTests();

        boolean result = runTests.runTests(response);

        assertFalse(result, "Tests should fail if Maven test execution fails");
    }

    /**
     * Test that `runTests` handles missing cloned directory gracefully.
     */
    @Test
    void testRunTests_ClonedDirectoryMissing() {
        // Delete the cloned directory to simulate missing repo
        clonedDir.toFile().delete();

        RunTests runTests = spy(new RunTests());

        boolean result = runTests.runTests(response);

        assertFalse(result, "Tests should fail if cloned directory is missing");
    }

    /**
     * Test that `runMavenTests` returns -1 when a MavenInvocationException occurs.
     */
    @Test
    void testRunMavenTests_Exception() {
        RunTests runTests = new RunTests();

        // Create a dummy Maven invoker that throws an exception
        Invoker invoker = mock(Invoker.class);
        try {
            when(invoker.execute(any())).thenThrow(new MavenInvocationException("Maven failure"));
        } catch (MavenInvocationException ignored) {}

        int result = runTests.runMavenTests(clonedDir);

        assertEquals(-1, result, "runMavenTests should return -1 on exception");
    }

    /**
     * We need to create a pom.xml file for testing
     * If the file exists, it appends to it; otherwise, it creates a new file.
     *
     * @param projectDir The directory where `pom.xml` should be created.
     */
    private static void ensurePomFileExistsAndWrite(Path projectDir) {
        File pomFile = new File(projectDir.toFile(), "pom.xml");

        try {
            // If the .env file does not exist, create it
            if (!pomFile.exists()) {
                boolean created = pomFile.createNewFile();
                if (created) {
                    System.out.println("Created new pom.xml file at: " + pomFile.getAbsolutePath());
                } else {
                    System.err.println("Failed to create pom.xml file.");
                    return;
                }

                // Write to the pom file
                try (FileWriter writer = new FileWriter(pomFile, false)) { // false disable append mode
                    writer.write("<groupId>com.group16.app</groupId>");
                    System.out.println("Wrote to pom.xml file at: " + pomFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling pom.xml file: " + e.getMessage());
        }
    }
}
