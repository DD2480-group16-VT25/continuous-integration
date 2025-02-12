package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.JSONObject;

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class ContinuousIntegrationServer extends AbstractHandler {
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException {
        
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String checkPayload = request.getParameter("payload");

        if (checkPayload == null || checkPayload.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Empty JSON in ContinuousIntegrationServer.java");
            response.getWriter().println("If you haven't pushed anything then you don't have to worry");
        }

        JSONObject checkjson;

        try {
            checkjson = new JSONObject(checkPayload);
            System.out.println("Received JSON CIS 41: " + checkjson.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid JSON format in ContinuousIntegrationServer.java");
            response.getWriter().println("If you haven't pushed anything then you don't have to worry");
        }

        // for example
        // 1st clone your repository
        // 2nd compile the code
        
        // 3rd run tests
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            RunTests.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("Error with webhook post");
        }

        // 4th notify the result        
        String payload = request.getParameter("payload");
        String requestURL = request.getRequestURL().toString();

        JSONObject json;
        try {
            json = new JSONObject(payload);
            System.out.println("Received JSON CIS 67: " + json.toString());
            // JSONObject json = new JSONObject(payload);
            String owner = json.getJSONObject("repository").getJSONObject("owner").getString("login");
            String repo = json.getJSONObject("repository").getString("name");
            String commitSha = json.getString("after");

            Notification.sendNotification(Status.PENDING, requestURL, owner, repo, commitSha);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid JSON format in ContinuousIntegrationServer.java");
            response.getWriter().println("If you haven't pushed anything then you don't have to worry");
        }
        
        response.getWriter().println("CI job done");
    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }
}
