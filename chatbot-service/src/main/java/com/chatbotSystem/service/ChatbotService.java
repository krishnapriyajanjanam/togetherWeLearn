package com.chatbotSystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill";

    private final Map<String, String> promptMemory = new HashMap<>();

    public ChatbotService() {
        loadPrompts();
    }

    // ✅ Load predefined prompts from JSON
    private void loadPrompts() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("chatbot-prompts.json");
            JsonNode jsonNode = objectMapper.readTree(resource.getInputStream());

            for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> category = it.next();

                for (Iterator<Map.Entry<String, JsonNode>> questionIt = category.getValue().fields(); questionIt.hasNext(); ) {
                    Map.Entry<String, JsonNode> question = questionIt.next();
                    String normalizedQuestion = normalizeText(question.getKey());
                    String answer = question.getValue().asText();
                    promptMemory.put(normalizedQuestion, answer);
                }
            }

            System.out.println("📌 Loaded " + promptMemory.size() + " prompts into memory.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Get response: First check local prompts, then Hugging Face AI
    public String getResponse(String userQuery) {
        String extractedQuery = extractQuery(userQuery);
        String normalizedQuery = normalizeText(extractedQuery);

        System.out.println("🔍 User Query: " + userQuery);
        System.out.println("🔍 Extracted Query: " + extractedQuery);
        System.out.println("🔍 Normalized Query: " + normalizedQuery);

        // 1️⃣ Check for an **exact match** in prompt memory
        if (promptMemory.containsKey(normalizedQuery)) {
            System.out.println("✅ Exact Match Found: " + normalizedQuery);
            return promptMemory.get(normalizedQuery);
        }

        // 2️⃣ Use **fuzzy matching** to find the best possible match
        String bestMatch = findClosestMatch(normalizedQuery);
        if (bestMatch != null) {
            System.out.println("✅ Fuzzy Matched to: " + bestMatch);
            return promptMemory.get(bestMatch);
        }

        // 3️⃣ If no match, call Hugging Face AI
        System.out.println("🛠 No match found in JSON, calling AI...");
        return askHuggingFaceAI(extractedQuery);
    }

    // ✅ Extract actual query from the request
    private String extractQuery(String rawQuery) {
        if (rawQuery.startsWith("{") && rawQuery.contains("query")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawQuery);
                return jsonNode.path("query").asText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rawQuery;
    }

    // ✅ Normalize text (lowercase + remove punctuation)
    private String normalizeText(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    // ✅ Find the closest match using fuzzy matching
    private String findClosestMatch(String userQuery) {
        int maxAllowedDistance = 3; // Allow slight variations in wording
        return promptMemory.keySet().stream()
                .filter(storedQuestion -> levenshteinDistance(userQuery, storedQuestion) <= maxAllowedDistance)
                .min(Comparator.comparingInt(storedQuestion -> levenshteinDistance(userQuery, storedQuestion)))
                .orElse(null);
    }

    // ✅ Levenshtein Distance for fuzzy matching
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    // ✅ Call Hugging Face AI for unknown queries
    private String askHuggingFaceAI(String userQuery) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(HF_API_URL);
            request.setHeader("Authorization", "Bearer " + huggingFaceApiKey);
            request.setHeader("Content-Type", "application/json");

            String requestBody = "{\"inputs\": \"" + userQuery + "\"}";
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpClient.execute(request).getEntity().getContent()))) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(reader);

                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    return jsonNode.get(0).path("generated_text").asText();
                }
                return "Sorry, I couldn't understand that.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing your request.";
        }
    }
}
