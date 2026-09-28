package com.prguardian.controller;

import com.prguardian.retrieval.CodeChunk;
import com.prguardian.retrieval.RetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Exists so retrieval can be demoed and tested on its own, independent of
 * the full PR review flow. Useful for the interview walk-through: hit this
 * endpoint with a snippet, show the relevant chunks it pulls back, before
 * ever touching the LLM call.
 */
@RestController
@RequestMapping("/api/retrieval")
public class RetrievalController {

    private final RetrievalService retrievalService;

    public RetrievalController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK) {

        List<CodeChunk> results = retrievalService.retrieveRelevantContext(query, topK);

        List<Map<String, Object>> response = results.stream()
                .map(chunk -> Map.<String, Object>of(
                        "filePath", chunk.getFilePath(),
                        "contentPreview", chunk.getContent().length() > 200
                                ? chunk.getContent().substring(0, 200) + "..."
                                : chunk.getContent()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}