package com.group16.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class ContinuousIntegrationServer extends AbstractHandler
{
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("application/json;charset=utf-8");

        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println("target");
        System.out.println(target);
        System.out.println("request");
        System.out.println(request);
        // response.getWriter().println(target);


        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code
        // 3rd run tests


        if ("POST".equalsIgnoreCase(request.getMethod())) {
            RunTests.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"error\": \"Error with webhook post\"}");
            
        }

        // 4th notify
        String payload = request.getParameter("payload");
        
        response.getWriter().println("{\"JOB\": \"CI job done\"}");
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
