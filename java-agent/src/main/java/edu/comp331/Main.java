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

    // list of current valid command line flags
    final private static Flag[] VALID_FLAGS = new Flag[] {
            new Flag("help", "h", "Show this help message"),
            new Flag("verbose", "v", "Enable verbose output"),
    };

    /**
     * Sends a prompt to the Gemini API and returns the response.
     * 
     * @param prompt  String containing the prompt to pass along
     * @param apiKey  String Gemini API key to use for the request
     * @param verbose boolean whether to print verbose output
     * @return String containing the response from the Gemini API
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    private static String sendPrompt(String prompt, String apiKey, boolean verbose)
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

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        int maxRetries = 5;
        int attemptNum = 0;

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            if (verbose) {
                System.out.println("Sending request to Gemini, attempt " + (attemptNum + 1) + "...");
            }

            HttpResponse<String> response;

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
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

            // If the request failed and is not retryable, or if we've exhausted our
            // retries, throw an exception
            if (!retryable || attemptNum >= maxRetries) {
                throw new java.io.IOException(
                        "Gemini request failed with HTTP status " + status
                                + ": " + response.body());
            }

            long delaySeconds = 1L << attemptNum; // exponential backoff: 1, 2, 4, 8, 16 seconds

            if (verbose) {
                System.out.println(
                        "Gemini returned HTTP " + status
                                + ". Retrying in " + delaySeconds + " second(s)...");
            }

            Thread.sleep(delaySeconds * 1000L);
            attemptNum++;

        }
    }

    /**
     * Print the available command-line flags.
     */
    private static void printFlags() {
        System.out.println("Usage: java -jar java-agent.jar [flags]");
        System.out.println("Usage: mvn exec:java -Dexec.args=\"[flags]\"");
        System.out.println("Flags:");
        for (Flag f : VALID_FLAGS) {
            System.out.printf("  --%s, -%s: %s%n", f.name(), f.shorthand(), f.description());
        }
    }

    /**
     * Print the help message.
     */
    private static void printHelp() {
        System.out.println(
                "\nThis program takes as a command line arguments a prompt and sends it to the Gemini API and prints the response.");
        System.out.println("You must set the GEMINI_API_KEY environment variable or add it to a .env file.\n");
        printFlags();
    }

    /**
     * Main entry point for the program. Parses command line arguments, loads
     * environment variables, and sends a prompt to the Gemini API.
     * 
     * @param args optional flags and a required prompt. Order does not matter.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        HashMap<String, Flag> flagMap = new HashMap<>();
        for (Flag flag : VALID_FLAGS) {
            flagMap.put("-" + flag.shorthand(), flag);
            flagMap.put("--" + flag.name(), flag);
        }
        boolean verbose = false;
        String prompt = "";

        for (String arg : args) {
            if (arg.startsWith("-")) {
                Flag flag = flagMap.get(arg);
                if (flag == null) {
                    System.err.println("Unknown flag: " + arg);
                    System.exit(1);
                }
                if (flag.name().equals("help")) {
                    printHelp();
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
                    System.exit(0);
                }
            }
        }

        if (verbose) {
            System.out.println("Verbose mode enabled.");
            System.out.println("Working directory: " + System.getProperty("user.dir"));
            System.out.println("Loading environment variables...");
        }

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
        if (verbose) {
            System.out.println("Agent selected: gemini-3.8-flash");
            System.out.println("API key found: " + (apiKey != null && !apiKey.isBlank()));
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY was not found. Add it to .env or your environment.");
        }

        // String prompt = "In one paragraph, explain why clear requirements help
        // software teams build better systems.";

        if (verbose) {
            System.out.println("Sending prompt: \"" + prompt + "\"");
        }
        String response = sendPrompt(prompt, apiKey, verbose);
        System.out.println("\nGemini response:");
        System.out.println(response);
    }
}
