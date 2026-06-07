package com.projectmanagementsaas.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectmanagementsaas.ai.entity.AiModel;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OllamaClient {
    private final RestClient restClient;
    private final boolean enabled;

    public OllamaClient(
            RestClient.Builder builder,
            @Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ai.ollama.enabled:false}") boolean enabled
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.enabled = enabled;
    }

    public String chat(AiModel model, String prompt) {
        if (!enabled) {
            return "Local AI fallback: " + summarize(prompt);
        }
        JsonNode response = restClient.post()
                .uri("/api/chat")
                .body(Map.of(
                        "model", model.ollamaName(),
                        "stream", false,
                        "messages", List.of(Map.of("role", "user", "content", prompt))))
                .retrieve()
                .body(JsonNode.class);
        JsonNode content = response == null ? null : response.at("/message/content");
        return content == null || content.isMissingNode() ? "" : content.asText();
    }

    public List<Double> embed(String input) {
        if (!enabled) {
            return deterministicEmbedding(input);
        }
        JsonNode response = restClient.post()
                .uri("/api/embeddings")
                .body(Map.of("model", "nomic-embed-text", "prompt", input))
                .retrieve()
                .body(JsonNode.class);
        JsonNode embedding = response == null ? null : response.get("embedding");
        if (embedding == null || !embedding.isArray()) {
            return deterministicEmbedding(input);
        }
        java.util.ArrayList<Double> vector = new java.util.ArrayList<>();
        embedding.forEach(value -> vector.add(value.asDouble()));
        return vector;
    }

    private List<Double> deterministicEmbedding(String input) {
        double[] vector = new double[16];
        String value = input == null ? "" : input.toLowerCase();
        for (int i = 0; i < value.length(); i++) {
            vector[i % vector.length] += value.charAt(i) / 255.0;
        }
        return java.util.Arrays.stream(vector).boxed().toList();
    }

    private String summarize(String prompt) {
        String normalized = prompt == null ? "" : prompt.replaceAll("\\s+", " ").trim();
        return normalized.length() > 600 ? normalized.substring(0, 600) : normalized;
    }
}
