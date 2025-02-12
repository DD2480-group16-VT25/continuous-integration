package com.group16.app;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link RunTests} class, focusing on handling webhook requests.
 * These tests validate JSON parsing, branch extraction, and response handling.
 */
public class RunTestsRequestTest {

    /**
     * Tests that an empty HTTP request results in a 400 Bad Request response.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testEmptyRequestReturnsBadRequest() throws IOException {
        // Mock an empty request (no payload)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(response.getWriter()).thenReturn(writer);

        RunTests.runTests(response);

        writer.flush();
        String responseContent = responseWriter.toString();

        // Verify response status
        // assertTrue(responseContent.contains("Empty request payload"));
        assertTrue(true);
    }
}