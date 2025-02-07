package com.group16.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.github.cdimascio.dotenv.Dotenv;

public class Notification {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_PAT = dotenv.get("GITHUB_PAT");

    public static void sendNotification(Status status, String owner, String repo, String commitSha) {
        String body = String.format("""
                {
                    "state": "%s",
                    "target_url": "http://ci-server.example.com/build/123",
                    "description": "%s",
                    "context": "continuous-integration/jetty"
                }""",
                status.toString().toLowerCase(),
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
            System.out.println("GitHub Response: " + response.statusCode() + " - " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}