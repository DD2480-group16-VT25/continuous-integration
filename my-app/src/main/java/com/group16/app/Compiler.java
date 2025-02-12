package com.group16.app;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Compiler{
    static Path tempDir;

    /*
     * compileProj clones a Git repository from a specified branch and repo URL.
     * It then compiles the code with maven to see if it can successfully build.
     * @response a HttpServletResponse to write the response to
     * @param repoUrl a String containing the URL of the repository to clone
     * @param branchName a String containing the name of the branch to clone
     * @author Marcus Odin
     */
    public static boolean compileProj(HttpServletResponse response, String repoUrl, String branchName) throws IOException {

        // Clones the to a temporary directory.
        System.out.println("Cloning repository: " + repoUrl);
        boolean cloneSuccess = cloneRepo(repoUrl, branchName, "cloned-repo");
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
                return true;
                
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            response.getWriter().println("{\"message\": \"CI job done\", \"repo\": \"" + repoUrl + "\", \"success\": false}");
            return false;
        }
        return false;
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
}