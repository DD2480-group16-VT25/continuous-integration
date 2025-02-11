package com.group16.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Notification class to send status notifications to GitHub for a given commit.
 * 
 * Note: GITHUB_PAT is a Personal Access Token (PAT) for the GitHub API, which
 * needs to be stored in a .env file.
 */
public class Notification {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_PAT = dotenv.get("GITHUB_PAT");

    /**
     * Sends a notification to GitHub about the build/test status of a commit.
     * This method creates and sends a POST request to the GitHub API to update the
     * status of a specific commit.
     *
     * @param status     The build/test status (SUCCESS, FAILURE, ERROR or PENDING)
     *                   to be reported to GitHub
     * @param requestURL The URL for more information about the build/test
     * @param owner      The owner (user or organization) of the GitHub repository
     * @param repo       The name of the GitHub repository
     * @param commitSha  The SHA hash of the commit to update the status for
     */
    public static void sendNotification(Status status, String requestURL, String owner, String repo, String commitSha)
            throws RuntimeException {
        String body = String.format("""
                {
                    "state": "%s",
                    "target_url": "%s",
                    "description": "%s",
                    "context": "continuous-integration/jetty"
                }""",
                status.toString().toLowerCase(),
                requestURL,
                status == Status.SUCCESS ? "Build succeeded" : "Build failed");

        String url = String.format("%s/repos/%s/%s/statuses/%s", GITHUB_API_URL, owner, repo, commitSha);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + GITHUB_PAT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(String.format("GitHub Response for %s: %d - %s",
                    request.method(), response.statusCode(), response.body()));

            if (response.statusCode() != 201) { // GitHub API returns 201 for successful POST requests
                throw new RuntimeException("Failed to send notification to GitHub. Status code: " +
                        response.statusCode() + ", Response: " + response.body());
            }
        } catch (Exception e) {
            // Print error message to stderr and rethrow as a RuntimeException
            System.err.println("Error sending GitHub notification: " + e.getMessage());
            throw new RuntimeException("Failed to update GitHub status to " + status.toString().toLowerCase(), e);
        }
    }

}