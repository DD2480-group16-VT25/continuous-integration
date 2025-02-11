package com.group16.app; 

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
 
public class CompilerTest {

    @Test
    public void CompilerClonesAndCompilesSuccessfully() throws IOException{
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String jsonPayload = "{"
                + "\"ref\":\"refs/heads/compile\","
                + "\"repository\": {"
                + "  \"clone_url\": \"https://github.com/DD2480-group16-VT25/continuous-integration.git\""
                + "}"
                + "}";

        // Stub the request.getReader() to return a BufferedReader over the JSON payload.
        BufferedReader reader = new BufferedReader(new StringReader(jsonPayload));
        when(request.getReader()).thenReturn(reader);
        
        // Prepare a StringWriter and PrintWriter to capture the output from response.getWriter().
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        Compiler.compileProj(request, response);
    }

    @Test
    public void CompilerClonesAndCompilesUnsuccessfullyWithBadPayload(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertThrows(NullPointerException.class, () -> {
            Compiler.compileProj(request, response);
        });
    }
}
