package com.group16.app;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.nio.file.Files;
import java.nio.file.Path;

public class Compiler{
    static Path tempDir;

    public static void compileProj(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        String repoUrl = extractRepoUrl(jsonPayload.toString());
        if (repoUrl == null) {
            response.getWriter().println("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        System.out.println("Cloning repository: " + repoUrl);
        boolean cloneSuccess = cloneRepo(repoUrl, "cloned-repo");
        response.getWriter().println("{\"message\": \"CI job done\", \"repo\": \"" + repoUrl + "\", \"success\": " + cloneSuccess + "}");
        if(cloneSuccess){
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("mvn", "clean", "compile");
            processBuilder.directory(tempDir.toFile());
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
                
                // An exit code of 0 typically indicates success.
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String extractRepoUrl(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return jsonObject.getAsJsonObject("repository").get("clone_url").getAsString();
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    private static boolean cloneRepo(String repoUrl, String cloneDir) throws IOException {
        try {
            tempDir = Files.createTempDirectory("tempRepo");

            System.out.println("Temporary directory created at: " + tempDir.toAbsolutePath());
            
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(tempDir.toFile())
                .call();
            System.out.println("Repository cloned successfully into " + cloneDir);
            return true;
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
            return false;
        }
    }


}