package com.group16.app;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.maven.shared.invoker.*;

/**
 * Handles incoming webhook requests and triggers Maven test execution.
 * Ensures tests only run on the 'assessment' branch.
 */
public class RunTests {
    /**
     * Handles incoming HTTP requests from GitHub webhooks.
     * Reads the request payload, extracts the branch name,.
     *
     * @param branch The branch name from the payload.
     * @param response The HTTP response object to send back results.
     * @return true if the tests pass, false if tests fail
     * @throws IOException If there is an issue reading the request body.
     */
    public static boolean runTests(HttpServletResponse response) {
        try {
            // Run Maven Tests
            int testResult = runMavenTests();
            
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
     * Executes Maven tests using the Maven Shared Invoker API.
     *
     * @return Exit code from the Maven test execution (0 if successful, >0 if tests fail).
     */
    public static int runMavenTests() {
        try {
            Invoker invoker = new DefaultInvoker();

            // Auto-detect Maven Home
            String mavenHome = System.getenv("MAVEN_HOME");
            if (mavenHome == null || mavenHome.isEmpty()) {
                mavenHome = System.getProperty("maven.home");
            }
            if (mavenHome == null || mavenHome.isEmpty()) {
                mavenHome = "/usr/share/maven"; // Default for Linux/macOS
            }
            invoker.setMavenHome(null); // Set Maven installation directory
            // invoker.setMavenHome(new File(mavenHome)); // Set Maven installation directory
            invoker.setWorkingDirectory(new File(".")); // Project root directory

            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(new File("pom.xml"));
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
