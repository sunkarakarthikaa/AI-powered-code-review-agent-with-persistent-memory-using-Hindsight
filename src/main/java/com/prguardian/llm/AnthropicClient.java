package com.prguardian.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Anthropic's /v1/messages endpoint. Deliberately dumb:
 * takes a system prompt + user message, returns raw text. All the
 * intelligence (what to ask, how to validate the answer) lives outside
 * this class, in PromptBuilder and ReviewVerdictParser respectively.
 * That separation means we could swap this for OpenAI/Gemini/a local
 * model by writing one new class, same as EmbeddingClient in Step 2.
 */
@Component
public class AnthropicClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public AnthropicClient(
            @Value("${openrouter.api-key:}") String apiKey,
            @Value("${llm.model}") String model) {

        this.apiKey = apiKey;
        this.model = model;

        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }

    @SuppressWarnings("unchecked")
    public String call(String systemPrompt, String userMessage) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 2000,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", systemPrompt
                        ),
                        Map.of(
                                "role", "user",
                                "content", userMessage
                        )
                )
        );

        Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.get("choices");

        Map<String, Object> message =
                (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}