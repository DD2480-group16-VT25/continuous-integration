package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.json.JSONObject;
import java.io.File;

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

        System.out.println("Webhook received for branch: " + branch);

        if ("feat/testing".equals(branch)) { // testing
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
