package com.group16.app;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        // A part of a valid payload
        String payload = "{\"ref\": \"refs/heads/assessment\"}";
        JSONObject json = new JSONObject(payload);

        // Get branch from payload
        String branch = json.optString("ref", "unknown");

        // Assert the correct branch is extracted
        assertEquals("refs/heads/assessment", branch);
    }

    /**
     * Tests that an empty JSON payload returns the default branch value.
     */
    @Test
    void testExtractBranchFromInvalidPayload() {
        // A part of a valid payload
        JSONObject json = new JSONObject("{}");

        // Get branch from payload
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
        // Mock an empty request
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(response.getWriter()).thenReturn(writer);

        int statusCode = RunTests.handleRequest(request, response);

        writer.flush();
        String jsonResponse = responseWriter.toString();

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
        assertTrue(jsonResponse.contains("Empty JSON payload"));
    }

    /**
     * Tests that an invalid JSON request results in a 400 Bad Request response.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testInvalidJsonReturnsBadRequest() throws IOException {
        // Mock an empty request
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);

        String invalidJson = "{ invalid-json }";  // Missing quotes

        BufferedReader reader = new BufferedReader(new StringReader(invalidJson));

        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        int statusCode = RunTests.handleRequest(request, response);

        writer.flush();
        String jsonResponse = responseWriter.toString();

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
        assertTrue(jsonResponse.contains("Invalid JSON format"));
    }
}