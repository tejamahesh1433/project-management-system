package com.projectmanagementsaas.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public EmbeddingService(OllamaClient ollamaClient, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public List<Double> embed(String input) {
        return ollamaClient.embed(input);
    }

    public String toJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize embedding", exception);
        }
    }

    public List<Double> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    public double cosine(List<Double> left, List<Double> right) {
        int size = Math.min(left.size(), right.size());
        if (size == 0) {
            return 0.0;
        }
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < size; i++) {
            dot += left.get(i) * right.get(i);
            leftNorm += left.get(i) * left.get(i);
            rightNorm += right.get(i) * right.get(i);
        }
        return leftNorm == 0 || rightNorm == 0 ? 0.0 : dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
