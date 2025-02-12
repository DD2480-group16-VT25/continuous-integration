package com.group16.app;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Handles incoming webhook requests and triggers Maven test execution.
 * Ensures tests only run on the 'assessment' branch.
 */
public class RunTests {
    /**
     * Handles incoming HTTP requests from GitHub webhooks.
     * Reads the request payload, extracts the branch name,.
     *
     * @param response The HTTP response object to send back results.
     * @return true if the tests pass, false if tests fail
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
            invoker.setMavenHome(null); // Set Maven installation directory
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
