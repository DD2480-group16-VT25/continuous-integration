package com.group16.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

/**
 * This class is the main class of the CI server. It listens for POST requests
 * from the GitHub webhook and processes them. It extracts the necessary
 * information from the payload, compiles the project, runs the tests and sends
 * a notification to the notification API.
 */
public class ContinuousIntegrationServer extends AbstractHandler {
    /**
     * Method to handle POST requests to the CI server. It extracts the necessary
     * information from the payload, compiles the project, runs the tests and sends
     * a notification to the notification API.
     * 
     * @param target      The target of the request
     * @param baseRequest The original unwrapped request object
     * @param request     The HttpServletRequest object
     * @param response    The response as a HttpServletResponse object
     * 
     * @throws IOException      If an input or output exception occurs
     * @throws ServletException If a servlet exception occurs
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        baseRequest.setHandled(true);
        response.setStatus(HttpServletResponse.SC_OK);

        // Only POST requests are allowed
        if (!request.getMethod().equals("POST")) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.getWriter().println("Only POST requests are allowed");
            return;
        }

        // If ping request, return 200 OK
        if (request.getHeader("X-GitHub-Event").equals("ping")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Extracting what is neccesary from the payload.
        JSONObject json;
        String payload, requestURL, owner, repo, commitSha, repoURL, branch;        
        try {
            payload = request.getParameter("payload");
            requestURL = request.getRequestURL().toString();
            json = new JSONObject(payload);
            owner = json.getJSONObject("repository").getJSONObject("owner").getString("login");
            repo = json.getJSONObject("repository").getString("name");
            commitSha = json.getString("after");
            repoURL = json.getJSONObject("repository").getString("clone_url");
            branch = json.getString("ref");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Bad request, missing one or more required parameters in payload");
            return;
        }

        // Respond to the webhook to prevent timeout
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("CI job started");

        // Status PENDING while we are building and testing
        new Thread(() -> {
            try {
                Notification.sendNotification(Status.PENDING, requestURL, owner, repo, commitSha);
            } catch (Exception e) {
                // Something went wrong with the notification API call
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                System.err.println("Error accessing the notification API");
                return;
            }

            // Compile and run tests
            boolean compileResultOK, testResultOK;
            try {
                compileResultOK = Compiler.compileProj(response, repoURL, branch);
                System.out.println("Compile result: " + compileResultOK);
                testResultOK = RunTests.runTests(response);
                System.out.println("Test result: " + testResultOK);
            } catch (Exception e) {
                // Something went wrong with the compilation or test running
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                System.err.println("Error compiling or running tests: " + e.getMessage());

                try {
                    Notification.sendNotification(Status.ERROR, requestURL, owner, repo, commitSha);
                } catch (Exception notificationError) {
                    // If notification fails, log it but keep the original error response
                    System.err.println(
                            "Additionally, failed to send error notification: " + notificationError.getMessage());
                }
                return;
            }
            
            // Both compile and test methods ran without exceptions, so we can
            // update the status of the commit according to results
            try {
                if (compileResultOK && testResultOK) {
                    Notification.sendNotification(Status.SUCCESS, requestURL, owner, repo, commitSha);
                } else {
                    Notification.sendNotification(Status.FAILURE, requestURL, owner, repo, commitSha);
                }
            } catch (Exception e) {
                // Something went wrong with the notification API call
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                System.err.println("Error accessing the notification API: " + e.getMessage());
                return;
            }

            System.out.println("CI job done");
        }).start();
    }
 
    /**
     * Main method to start the server.
     * @param args Command line arguments
     * @throws Exception If the server fails to start
     */
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }
}
