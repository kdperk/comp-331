package edu.comp331;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

public class Main {

    final private static Flag[] VALID_FLAGS = new Flag[] {
            new Flag("help", "h", "Show this help message"),
            new Flag("verbose", "v", "Enable verbose output"),
    };

    public static String sendPrompt(String prompt, String apiKey, boolean verbose)
            throws java.io.IOException, InterruptedException {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank.");
        }
        if (prompt.length() > 5000) {
            throw new IllegalArgumentException("Prompt must not exceed 5000 characters.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Gemini API key must not be null or blank.");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent";

        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

        HttpClient client = HttpClient.newHttpClient();
        int maxRetries = 5;
        int attempt = 0;

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            if (verbose) {
                System.out.println("Sending request to Gemini, attempt " + (attempt + 1) + "...");
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                JSONObject json = new JSONObject(response.body());

                return json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }

            boolean retryable = status == 500 || status == 502 || status == 503;

            if (!retryable || attempt >= maxRetries) {
                throw new java.io.IOException(
                        "Gemini request failed with HTTP status " + status
                                + ": " + response.body());
            }

            long delaySeconds = 1L << attempt;

            if (verbose) {
                System.out.println(
                        "Gemini returned HTTP " + status
                                + ". Retrying in " + delaySeconds + " second(s)...");
            }

            Thread.sleep(delaySeconds * 1000L);
            attempt++;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        HashMap<String, Flag> flagMap = new HashMap<>();
        for (Flag flag : VALID_FLAGS) {
            flagMap.put(flag.shorthand(), flag);
            flagMap.put(flag.name(), flag);
        }
        boolean verbose = false;
        String prompt = "";

        for (String arg : args) {
            if (arg.startsWith("--")) {
                String flagName = arg.substring(2);
                Flag flag = flagMap.get(flagName);
                if (flag == null) {
                    System.err.println("Unknown flag: " + arg);
                    System.exit(1);
                }
                if (flag.name().equals("help")) {
                    System.out.println("Usage: java -jar java-agent.jar [flags]");
                    System.out.println("Flags:");
                    for (Flag f : VALID_FLAGS) {
                        System.out.printf("  --%s, -%s: %s%n", f.name(), f.shorthand(), f.description());
                    }
                    System.exit(0);
                }
                if (flag.name().equals("verbose")) {
                    verbose = true;
                }
            } else if (arg.startsWith("-")) {
                String flagShorthand = arg.substring(1);
                Flag flag = flagMap.get(flagShorthand);
                if (flag == null) {
                    System.err.println("Unknown flag: " + arg);
                    System.exit(1);
                }
                if (flag.name().equals("help")) {
                    System.out.println("Usage: java -jar java-agent.jar [flags]");
                    System.out.println("Flags:");
                    for (Flag f : VALID_FLAGS) {
                        System.out.printf("  --%s, -%s: %s%n", f.name(), f.shorthand(), f.description());
                    }
                    System.exit(0);
                }
                if (flag.name().equals("verbose")) {
                    verbose = true;
                }
            } else {
                if (prompt.isEmpty()) {
                    prompt = arg;
                } else {
                    System.out.println("Multiple prompts provided");
                    System.exit(1);
                }
            }
        }

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
        System.out.println("API key found: " + (apiKey != null && !apiKey.isBlank()));
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY was not found. Add it to .env or your environment.");
        }

        // String prompt = "In one paragraph, explain why clear requirements help
        // software teams build better systems.";

        if (verbose) {
            System.out.println("Sending prompt: " + prompt);
        }
        String response = sendPrompt(prompt, apiKey, true);
        System.out.println("\nGemini response:");
        System.out.println(response);
    }
}
