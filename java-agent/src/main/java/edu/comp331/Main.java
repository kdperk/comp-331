package edu.comp331;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    final private static Flag[] VALID_FLAGS = new Flag[] {
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

        System.out.println("Sending request to Gemini...");
        String text = GeminiClient.generateText(prompt, apiKey);

        System.out.println("\nGemini response:");
        System.out.println(text);
    }
}
