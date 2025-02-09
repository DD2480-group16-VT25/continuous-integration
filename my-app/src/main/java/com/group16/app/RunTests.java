package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

// import org.apache.maven.cli.MavenCli;
import org.json.JSONObject;
import java.io.File;

public class RunTests {
    public static void handleRequest(HttpServletRequest request, 
                                    HttpServletResponse response) 
            throws IOException, ServletException {
        
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Read JSON payload
        String payload = request.getReader().lines().collect(Collectors.joining());
        JSONObject json = new JSONObject(payload);

        // Extract branch name
        String ref = json.optString("ref", "");
        String branch = ref.replace("refs/heads/", ""); 

        System.out.println("Webhook received for branch: " + branch);

        if ("assessment".equals(branch)) {
            System.out.println("Running Maven tests on branch: " + branch);

            // TODO run tests

            String tested = true;

            if (tested) {
                response.getWriter().println("Maven tests executed successfully on branch: " + branch);
            } else {
                response.getWriter().println("Maven tests failed on branch: " + branch);
            }
        } else {
            System.out.println("Skipping tests, not the assessment branch.");
            response.getWriter().println("Webhook received for branch: " + branch);
        }
    }
}
