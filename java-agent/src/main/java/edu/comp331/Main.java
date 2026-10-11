package edu.comp331;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {

    final private static Flag[] FLAGS = new Flag[] {
            new Flag("help", "h", "Show this help message"),
            new Flag("verbose", "v", "Enable verbose output"),
    };

    public static void main(String[] args) throws Exception {
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
        System.out.println("API key found: " + (apiKey != null && !apiKey.isBlank()));
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY was not found. Add it to .env or your environment.");
        }

        String prompt = "In one paragraph, explain why clear requirements help software teams build better systems.";

        JSONObject body = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        System.out.println("Sending request to Gemini...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("HTTP status: " + response.statusCode());
        System.out.println("Raw response:");
        System.out.println(response.body());

        int retries = 0;
        int waitTime = 2;
        while (response.statusCode() == 503 && retries++ < 5) {
            System.out.println("Gemini API is busy. Retrying in " + waitTime + " seconds...");
            Thread.sleep(waitTime * 1000);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP status: " + response.statusCode());
            System.out.println("Raw response:");
            System.out.println(response.body());
            waitTime *= 2;
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini API request failed with status "
                    + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());
        String text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        System.out.println("\nGemini response:");
        System.out.println(text);
    }
}
