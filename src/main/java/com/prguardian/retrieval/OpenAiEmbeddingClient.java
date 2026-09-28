package com.prguardian.retrieval;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Real implementation: calls OpenAI's embeddings endpoint.
 * (Any embedding provider works the same way here — Voyage AI, Cohere,
 * a self-hosted model behind an HTTP endpoint. Only this one class would
 * change.)
 *
 * text-embedding-3-small produces 1536-dimension vectors and is cheap
 * enough to call per-chunk without worrying about cost for a demo repo.
 */

@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final WebClient webClient;
    private final String apiKey;

    public OpenAiEmbeddingClient(
            @Value("${openrouter.api-key:}") String apiKey) {

        this.apiKey = apiKey;

        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public float[] embed(String text) {

        Map<String, Object> requestBody = Map.of(
                "model", "nvidia/nemotron-3-embed-1b:free",
                "input", text
        );

        Map<String, Object> response = webClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> data =
                (List<Map<String, Object>>) response.get("data");

        List<Double> vector =
                (List<Double>) data.get(0).get("embedding");

        float[] result = new float[vector.size()];

        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).floatValue();
        }

        return result;
    }
}