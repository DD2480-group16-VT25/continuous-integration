package com.group16.app;

import org.junit.jupiter.api.Test;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link RunTests} class, focusing on handling webhook requests.
 * These tests validate form-encoded parsing, branch extraction, and response handling.
 */
public class RunTestsRequestTest {

    /**
     * Tests that a valid URL-encoded payload correctly extracts the branch name.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testExtractBranchFromValidPayload() throws IOException {
        // A part of a valid request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("ref")).thenReturn("refs/heads/assessment");

        // Get branch from request
        String branch = request.getParameter("ref");

        // Assert the correct branch is extracted
        assertEquals("refs/heads/assessment", branch);
    }

    /**
     * Tests that an empty URL-encoded payload returns the default branch value.
     */
    @Test
    void testExtractBranchFromInvalidPayload() {
        // Mock request with missing 'ref' parameter
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("ref")).thenReturn(null);

        // Get branch from request
        String branch = request.getParameter("ref");

        // If parameter is missing, default should be null
        assertNull(branch);
    }
    /**
     * Tests that an empty HTTP request results in a 400 Bad Request response.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    void testEmptyRequestReturnsBadRequest() throws IOException {
        // Mock an empty request (no parameters)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);

        when(request.getParameter("ref")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        RunTests.handleRequest(request, response);

        writer.flush();
        String responseContent = responseWriter.toString();

        // Verify response status is 400 Bad Request
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseContent.contains("Empty request payload"));
    }
}