package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.maven.shared.invoker.*;

/**
 * Handles incoming webhook requests and triggers Maven test execution.
 * Ensures tests only run on the 'assessment' branch.
 */
public class RunTests {
    /**
     * Handles incoming HTTP requests from GitHub webhooks.
     * Reads the request payload, extracts the branch name, and runs tests only if the branch is 'assessment'.
     *
     * @param request  The incoming HTTP request containing the webhook payload.
     * @param response The HTTP response object to send back results.
     * @return HTTP status code (200 if successful, 400 if bad request, 500 if no response is written).
     * @throws IOException If there is an issue reading the request body.
     */
    public static int handleRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=utf-8");

            // Read request payload from the request body
            String payload = request.getParameter("payload");

            // Error if request is blank
            if (payload == null || payload.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Empty request payload");
                return HttpServletResponse.SC_BAD_REQUEST;
            }

            // JSONObject json = new JSONObject(payload);
            
            JSONObject json;
            try {
                json = new JSONObject(payload);
                System.out.println("Received JSON RT 45: " + json.toString());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid JSON format in RunTests.java");
                return HttpServletResponse.SC_BAD_REQUEST;
            }

            // Extract branch
            String branch = json.optString("ref", "no_branch");
            response.getWriter().println("Received branch: " + branch);
            if (branch.equals("refs/heads/assessment")) {
                response.setStatus(HttpServletResponse.SC_OK);

                // Run Maven Tests
                int testResult = runMavenTests();
                
                if (testResult == 0) {
                    response.getWriter().println("Tests passed");
                    return HttpServletResponse.SC_OK;
                } else {
                    response.getWriter().println("Tests failed, exit code = " + testResult);
                    return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                }
            } else {
                response.getWriter().println("Not the assessment branch");
                return HttpServletResponse.SC_OK;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
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
            invoker.setMavenHome(new File(mavenHome)); // Set Maven installation directory
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
