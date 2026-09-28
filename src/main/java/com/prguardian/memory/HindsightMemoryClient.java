package com.prguardian.memory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HindsightMemoryClient {

    private final RestClient client;
    private final String bankId;

    public HindsightMemoryClient(
            @Value("${hindsight.base-url}") String baseUrl,
            @Value("${hindsight.bank-id}") String bankId) {

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.bankId = bankId;
    }

    /**
     * RETAIN:
     * Stores useful knowledge from a completed PR review.
     */
    public void retain(String content, String documentId) {
        try {
            Map<String, Object> item = new HashMap<>();
            item.put("content", content);
            item.put("document_id", documentId);

            Map<String, Object> body = new HashMap<>();
            body.put("items", List.of(item));
            body.put("async", false);

            client.post()
                    .uri("/v1/default/banks/{bank}/memories", bankId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("[Hindsight] RETAIN successful for " + documentId);

        } catch (Exception e) {
            // Memory must not prevent the PR review itself.
            System.err.println(
                    "[Hindsight] RETAIN failed: " + e.getMessage());
        }
    }

    /**
     * RECALL:
     * Retrieves relevant knowledge from previous PR reviews.
     */
    @SuppressWarnings("unchecked")
    public List<String> recall(String query) {

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("max_tokens", 1000);
            body.put("budget", "low");

            Map<String, Object> response = client.post()
                    .uri("/v1/default/banks/{bank}/memories/recall", bankId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<String> memories = new ArrayList<>();

            if (response != null && response.get("results") instanceof List<?> results) {
                for (Object result : results) {
                    if (result instanceof Map<?, ?> resultMap) {
                        Object text = resultMap.get("text");

                        if (text != null && !text.toString().isBlank()) {
                            memories.add(text.toString());
                        }
                    }
                }
            }

            System.out.println(
                    "[Hindsight] RECALL returned "
                            + memories.size()
                            + " memories");

            return memories;

        } catch (Exception e) {
            // If Hindsight is temporarily unavailable,
            // the normal PR review can still continue.
            System.err.println(
                    "[Hindsight] RECALL failed: " + e.getMessage());

            return List.of();
        }
    }
}