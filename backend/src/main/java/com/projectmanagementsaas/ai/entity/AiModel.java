package com.projectmanagementsaas.ai.entity;

public enum AiModel {
    QWEN3("qwen3"),
    GEMMA3("gemma3"),
    PHI4_MINI("phi4-mini");

    private final String ollamaName;

    AiModel(String ollamaName) {
        this.ollamaName = ollamaName;
    }

    public String ollamaName() {
        return ollamaName;
    }
}
