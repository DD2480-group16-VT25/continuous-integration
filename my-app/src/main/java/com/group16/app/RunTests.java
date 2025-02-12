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
    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_PAT = dotenv.get("GITHUB_PAT");
    public static boolean runTests(HttpServletResponse response) {
        try {
            Path clonedDir = Compiler.tempDir.resolve("my-app");

            // // Write to .env file inside 'my-app'
            File envFile = new File(clonedDir.toFile(), ".env");
            try (FileWriter writer = new FileWriter(envFile, false)) {
                writer.write("GITHUB_PAT=" + GITHUB_PAT);
                System.out.println("Wrote to .env file: " + envFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error writing to .env file: " + e.getMessage());
                return false;
            }

            if (clonedDir == null) {
                System.out.println("Cloned directory not found.");
                return false;
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
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(null); // Set Maven installation directory
            invoker.setWorkingDirectory(clonedDir.toFile()); // Cloned directory

            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(new File(clonedDir.toFile(), "pom.xml"));
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
}
