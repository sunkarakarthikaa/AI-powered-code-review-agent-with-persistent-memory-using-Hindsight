package com.prguardian.llm;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class HindsightClient {

    private final WebClient webClient;

    private static final String BANK_ID = "pr-guardian";

    public HindsightClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8888")
                .build();
    }

    /**
     * RETAIN
     * Stores useful information from a completed PR review.
     */
    public void retain(String content) {
        try {
            Map<String, Object> item = Map.of(
                    "content", content,
                    "document_id", "pr-review"
            );

            Map<String, Object> body = Map.of(
                    "items", List.of(item),
                    "async", false
            );

            webClient.post()
                    .uri("/v1/default/banks/" + BANK_ID + "/memories")
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("[Hindsight] RETAIN successful");

        } catch (Exception e) {
            System.err.println("[Hindsight] RETAIN failed: " + e.getMessage());
        }
    }

    /**
     * RECALL
     * Retrieves relevant memories from previous PR reviews.
     */
    @SuppressWarnings("unchecked")
    public String recall(String query) {
        try {
            Map<String, Object> body = Map.of(
                    "query", query,
                    "max_tokens", 1500,
                    "budget", "low"
            );

            Map<String, Object> response = webClient.post()
                    .uri("/v1/default/banks/" + BANK_ID + "/memories/recall")
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("results") == null) {
                return "No previous memories found.";
            }

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) response.get("results");

            if (results.isEmpty()) {
                return "No previous memories found.";
            }

            StringBuilder memories = new StringBuilder();

            for (Map<String, Object> result : results) {
                Object text = result.get("text");

                if (text != null) {
                    memories.append("- ")
                            .append(text)
                            .append("\n");
                }
            }

            System.out.println("[Hindsight] RECALL successful - "
                    + results.size() + " memories");

            return memories.toString();

        } catch (Exception e) {
            System.err.println("[Hindsight] RECALL failed: " + e.getMessage());
            return "No previous memories available.";
        }
    }
}