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
     * Tests that a valid JSON payload correctly extracts the branch name.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testExtractBranchFromValidPayload() throws IOException {
        // Mock request with a JSON payload
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"ref\": \"refs/heads/assessment\"}")));

        // Read JSON payload
        String jsonPayload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject json = new JSONObject(jsonPayload);

        // Extract branch
        String branch = json.optString("ref", "unknown");

        // Assert the correct branch is extracted
        assertEquals("refs/heads/assessment", branch);
    }

    /**
     * Tests that an empty JSON payload returnsthe default branch value.
     */
    @Test
    void testExtractBranchFromInvalidPayload() throws IOException {
        // Mock request with empty JSON payload
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));

        // Read JSON payload
        String jsonPayload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject json = new JSONObject(jsonPayload);

        // Extract branch
        String branch = json.optString("ref", "unknown");

        // Assert the default value is returned
        assertEquals("unknown", branch);
    }

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

        RunTests.handleRequest(request, response);

        writer.flush();
        String responseContent = responseWriter.toString();

        // Verify response status
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseContent.contains("Empty request payload"));
    }

    /**
     * Tests that an invalid JSON request results in a 400 Bad Request response.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testInvalidRequestReturnsBadRequest() throws IOException {
        // Mock an invalid request
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{ invalid-json }"))); // Missing quotes
        when(response.getWriter()).thenReturn(writer);

        RunTests.handleRequest(request, response);

        writer.flush();
        String responseContent = responseWriter.toString();

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseContent.contains("Invalid request format"));
    }
}