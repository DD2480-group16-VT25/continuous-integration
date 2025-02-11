package com.group16.app;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Compiler{
    static Path tempDir;

    /*
     * compileProj clones a Git repository by excrating the url from a JSON payload.
     * It then compiles the code with maven to see if it can successfully build.
     * @param request a HttpServletRequest
     * @param response a HttpServletResponse
     * @author Marcus Odin
     */
    public static void compileProj(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Reads the JSON payload
        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        // Extracts the repo URL from the payload
        String repoUrl = extractRepoUrl(jsonPayload.toString());
        if (repoUrl == null) {
            response.getWriter().println("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // Clones the to a temporary directory.
        System.out.println("Cloning repository: " + repoUrl);
        boolean cloneSuccess = cloneRepo(repoUrl, extractBranchName(jsonPayload.toString()), "cloned-repo");
        response.getWriter().println("{\"message\": \"CI job done\", \"repo\": \"" + repoUrl + "\", \"success\": " + cloneSuccess + "}");

        // If cloning is successful use maven to compile the project
        if(cloneSuccess){
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("mvn", "clean", "compile");
            Path projectDir = tempDir.resolve("my-app"); // Goes into the maven project

            processBuilder.directory(projectDir.toFile());
            processBuilder.redirectErrorStream(true);
            try {
                // Start the Maven process
                Process process = processBuilder.start();
                
                // Read and output Maven process logs
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                
                // Wait for the process to complete and get the exit code
                int exitCode = process.waitFor();
                System.out.println("Maven process exited with code: " + exitCode);
                
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Extracts the git epository url from a JSON payload
    private static String extractRepoUrl(String json) {
        try {
            if (json.startsWith("payload=")) {
                json = json.substring("payload=".length());
                json = URLDecoder.decode(json, StandardCharsets.UTF_8);
            }
            JsonReader reader = new JsonReader(new StringReader(json));
            // Set strictness to LENIENT to accept malformed JSON
            reader.setStrictness(Strictness.LENIENT);
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject.getAsJsonObject("repository").get("clone_url").getAsString();
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    // Clones repository from a url
    private static boolean cloneRepo(String repoUrl, String branchName, String cloneDir) throws IOException {
        try {
            tempDir = Files.createTempDirectory("tempRepo");

            System.out.println("Temporary directory created at: " + tempDir.toAbsolutePath());
            
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(tempDir.toFile())
                .setBranch("refs/heads/" + branchName)
                .call();

            System.out.println("Repository cloned successfully into " + cloneDir);
            return true;
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
            return false;
        }
    }

    // Extracts branch name from JSON payload
    private static String extractBranchName(String json) {
        try {
            if (json.startsWith("payload=")) {
                json = json.substring("payload=".length());
                json = URLDecoder.decode(json, StandardCharsets.UTF_8);
            }
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setStrictness(Strictness.LENIENT);
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String ref = jsonObject.get("ref").getAsString();
            return ref.replace("refs/heads/", "");
        }
        catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }

    }


}