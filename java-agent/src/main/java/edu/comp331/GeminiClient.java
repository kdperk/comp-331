package edu.comp331;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class GeminiClient {

    private static final String MODEL = "gemini-3.8-flash";
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MILLIS = 2_000;

    private GeminiClient() {
    }

    public static String generateText(String prompt, String apiKey)
            throws IOException, InterruptedException {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be blank.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be blank.");
        }

        JSONObject body = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                        + MODEL + ":generateContent"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        long backoffMillis = INITIAL_BACKOFF_MILLIS;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException exception) {
                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
                sleepBeforeRetry(backoffMillis);
                backoffMillis *= 2;
                continue;
            }

            if (!isRetryable(response.statusCode()) || attempt == MAX_RETRIES) {
                break;
            }

            sleepBeforeRetry(backoffMillis);
            backoffMillis *= 2;
        }

        if (response == null || response.statusCode() < 200 || response.statusCode() >= 300) {
            int statusCode = response == null ? -1 : response.statusCode();
            throw new IOException("Gemini API request failed with status " + statusCode);
        }

        return new JSONObject(response.body())
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502
                || statusCode == 503 || statusCode == 504;
    }

    private static void sleepBeforeRetry(long backoffMillis) throws InterruptedException {
        Thread.sleep(backoffMillis);
    }
}
