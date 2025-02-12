package com.group16.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Unit test for the Notification class.
 */
public class NotificationTest {
    // This commit being tested has the message:
    // "feat: begin implementing notifications class".
    // And can be found at:
    // https://github.com/DD2480-group16-VT25/continuous-integration/commit/6a665d3a0189fa86980d2512b718f7b63fa6b3b0
    private static final String commitSha = "6a665d3a0189fa86980d2512b718f7b63fa6b3b0";
    private static final String repo = "continuous-integration";
    private static final String owner = "DD2480-group16-VT25";
    private static final String requestURL = "https://api.github.com/repos/DD2480-group16-VT25/continuous-integration/statuses/6a665d3a0189fa86980d2512b718f7b63fa6b3b0";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_PAT = dotenv.get("GITHUB_PAT");
    private static final String url = String.format("https://api.github.com/repos/%s/%s/statuses/%s", owner, repo,
            commitSha);

    /**
     * Get the status of a commit from the GitHub API.
     *
     * @return The status of the commit
     */
    private static Status getStatus() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + GITHUB_PAT)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();

        String state;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Convert HTTP response body to JSON array and get first status
            String responseBody = response.body();
            if (responseBody.startsWith("[")) {
                JSONObject json = new JSONObject(responseBody.substring(1, responseBody.length() - 1));
                state = json.getString("state");
            } else {
                JSONObject json = new JSONObject(responseBody);
                state = json.getString("state");
            }

            System.out.println(String.format("GitHub Response for %s: %d - %s",
                    request.method(), response.statusCode(), state));
        } catch (Exception e) {
            e.printStackTrace();
            return Status.ERROR;
        }

        return Status.valueOf(state.toUpperCase()); // Convert to Status enum
    }

    /**
     * (Re)set the commit status to PENDING before each test.
     */
    @BeforeEach
    public void setup() {
        Notification.sendNotification(Status.PENDING, requestURL, owner, repo, commitSha);
        assertEquals(getStatus(), Status.PENDING); // Implicitly tests the case of PENDING status
    }

    @Test
    public void setStatusSuccess() {
        Notification.sendNotification(Status.SUCCESS, requestURL, owner, repo, commitSha);
        assertEquals(getStatus(), Status.SUCCESS);
    }

    @Test
    public void setStatusFailure() {
        Notification.sendNotification(Status.FAILURE, requestURL, owner, repo, commitSha);
        assertEquals(getStatus(), Status.FAILURE);
    }

    @Test
    public void setStatusError() {
        Notification.sendNotification(Status.ERROR, requestURL, owner, repo, commitSha);
        assertEquals(getStatus(), Status.ERROR);
    }

    @Test
    public void invalidOwnerThrowsException() {
        String invalidOwner = "invalid-owner";
        assertThrows(RuntimeException.class, () -> {
            Notification.sendNotification(Status.SUCCESS, requestURL, invalidOwner, repo, commitSha);
        });
    }

    @Test
    public void invalidRepoThrowsException() {
        String invalidRepo = "invalid-repo";
        assertThrows(RuntimeException.class, () -> {
            Notification.sendNotification(Status.SUCCESS, requestURL, owner, invalidRepo, commitSha);
        });
    }

    @Test
    public void invalidCommitShaThrowsException() {
        String invalidCommitSha = "invalid-sha";
        assertThrows(RuntimeException.class, () -> {
            Notification.sendNotification(Status.SUCCESS, requestURL, owner, repo, invalidCommitSha);
        });
    }
}
