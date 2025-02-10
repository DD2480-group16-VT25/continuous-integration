package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.maven.cli.MavenCli;
import org.json.JSONObject;
import java.io.File;

/**
 * Handles the request and check if it is from the assessment branch (for now on testing branch for testing purposes)
 */

public class RunTests {
    public static void handleRequest(HttpServletRequest request, 
                                    HttpServletResponse response) 
            throws IOException, ServletException {
        
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Get JSON payload
        String payload = request.getReader().lines().collect(Collectors.joining());
        JSONObject json = new JSONObject(payload);

        // Get branch name
        String ref = json.optString("ref", "");
        String branch = ref.replace("refs/heads/", ""); 

        // Some testing output - will be removed
        System.out.println("1 Webhook received for branch: " + branch);
        System.out.println("++++++");
        System.out.println(branch);
        System.out.println("feat/testing");
        System.out.println("++++++");

        if ("feat/testing".equals(branch)) { // will be assessment
            System.out.println("Running Maven tests on branch: " + branch);

            int result = runMavenTests();

            System.out.println("result" + result);

            if (result == 0) {
                // if the test are successful
                response.getWriter().println("{\"success\": \"Maven tests executed successfully\"}");
            } else {
                // if tests fail
                response.getWriter().println("{\"failure\": \"Maven tests failed\"}");
            }
        } else {
            // not on the assessment branch, maybe unnecessary 
            response.getWriter().println("{\"testSkipped\": \"not the assessment branch\"}");
        }
    }

    /**
     * Runs Maven tests using MavenCli API.
     * @return 0 if tests pass, non-zero if they fail.
     */
    private static int runMavenTests() {
        try {
            // Set up MavenCli
            MavenCli cli = new MavenCli();
            File projectDirectory = new File(".").getAbsoluteFile();
            System.out.println("Running Maven tests in directory: " + projectDirectory.getAbsolutePath());

            System.setProperty("maven.multiModuleProjectDirectory", projectDirectory.getAbsolutePath());

            // Run Maven tests
            // -X for debugging tests
            // result = 0 for pass, otherwise fail
            int result = cli.doMain(new String[]{"test", "-Dtest=com.group16.app.AppTest", "-X"}, projectDirectory.getAbsolutePath(), System.out, System.err);

            System.out.println("Maven test finished with exit code: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Failure
        }
    }
}
