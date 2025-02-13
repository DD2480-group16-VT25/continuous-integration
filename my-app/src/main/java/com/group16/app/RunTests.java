package com.group16.app;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Collections;
import io.github.cdimascio.dotenv.Dotenv;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * This class executes the automated tests in the cloned repository
 * using Maven.
 */
public class RunTests {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_PAT = dotenv.get("GITHUB_PAT");
    /**
     * Runs the test suite for the cloned repository.
     *
     * This method identifies the correct directory for the cloned project,
     * then executes the Maven test command. It sends an appropriate response
     * indicating whether the tests passed or failed.
     *
     * @param response The {@link HttpServletResponse} object used to send results
     *                 back to the client.
     * @return {@code true} if tests pass successfully, {@code false} if they fail.
     */
    public static boolean runTests(HttpServletResponse response) {
        try {
            Path clonedDir = Compiler.tempDir.resolve("my-app");

            // Check if directory is found
            if (clonedDir == null) {
                System.out.println("Cloned directory not found.");
                return false;
            }

            if (GITHUB_PAT != null && !GITHUB_PAT.isEmpty()) {
                ensureEnvFileExistsAndWrite(clonedDir);
            } else {
                System.err.println("GITHUB_PAT environment variable is missing.");
            }

            System.out.println("Running tests in directory: " + clonedDir.toAbsolutePath());

            // Run Maven Tests
            int testResult = runMavenTests(clonedDir);
            
            // Report if the tests passed
            if (testResult == 0) {
                response.getWriter().println("Tests passed");
                return true;
            } else {
                response.getWriter().println("Tests failed, exit code = " + testResult);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes Maven tests in the specified project directory.
     *
     * This method uses the Maven Shared Invoker API to run tests within the
     * cloned repository. It sets the correct working directory and ensures Maven
     * output is logged.
     *
     * @param clonedDir The {@link Path} to the cloned repository where `pom.xml`
     *                  is located.
     * @return The exit code from the Maven test execution: {@code 0} if successful,
     *         greater than {@code 0} if tests fail, and {@code -1} in case of an error.
     */
    public static int runMavenTests(Path clonedDir) {
        try {
            if (clonedDir == null) {
                System.out.println("Cloned directory not found.");
                return -1;
            }

            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(null); // Set Maven installation directory
            invoker.setWorkingDirectory(clonedDir.toFile()); // Cloned directory

            InvocationRequest request = new DefaultInvocationRequest();

            File pomFile = new File(clonedDir.toFile(), "pom.xml");

            // Check if the pom.xml file exists before proceeding
            if (!pomFile.exists()) {
                System.err.println("Error: pom.xml not found in " + clonedDir.toAbsolutePath());
                return -1; // Return a failure code
            }

            request.setPomFile(pomFile);
            request.setGoals(Collections.singletonList("test")); // Run tests

            // Capture Maven output
            request.setOutputHandler(System.out::println);

            InvocationResult result = invoker.execute(request);

            if (result.getExitCode() == 0) {
                System.out.println("Tests passed successfully.");
            } else {
                System.err.println("Tests failed with exit code: " + result.getExitCode());
            }

            return result.getExitCode();

        } catch (MavenInvocationException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Writes the GITHUB_PAT into the `.env` file inside the cloned project.
     * If the file exists, it appends to it; otherwise, it creates a new file.
     *
     * @param projectDir The directory where `.env` should be created.
     */
    public static void ensureEnvFileExistsAndWrite(Path projectDir) {
        File envFile = new File(projectDir.toFile(), ".env");

        try {
            // If the .env file does not exist, create it
            if (!envFile.exists()) {
                boolean created = envFile.createNewFile();
                if (created) {
                    System.out.println("Created new .env file at: " + envFile.getAbsolutePath());
                } else {
                    System.err.println("Failed to create .env file.");
                    return;
                }
            }

            // Append or write to the .env file
            try (FileWriter writer = new FileWriter(envFile, true)) { // true enables append mode
                writer.write("GITHUB_PAT=" + GITHUB_PAT);
                System.out.println("Wrote GITHUB_PAT to .env file at: " + envFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Error handling .env file: " + e.getMessage());
        }
    }
}
