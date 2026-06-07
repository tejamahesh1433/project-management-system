package com.projectmanagementsaas.ai.dto;

import com.projectmanagementsaas.ai.entity.RagSourceType;
import java.util.UUID;

public record AiSearchResult(
        UUID sourceId,
        RagSourceType sourceType,
        String title,
        String snippet,
        double score
) {
}
