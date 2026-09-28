package com.prguardian.retrieval;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service the rest of the app calls to do retrieval — everything
 * else (VectorStore, EmbeddingClient, CosineSimilarity) is an
 * implementation detail behind this one method.
 */
@Service
public class RetrievalService {

    private static final int DEFAULT_TOP_K = 3;

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    public RetrievalService(EmbeddingClient embeddingClient, VectorStore vectorStore) {
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Given free-text (a PR diff, a description, anything), embed it and
     * return the most relevant existing code/policy chunks.
     */
    public List<CodeChunk> retrieveRelevantContext(String query) {
        return retrieveRelevantContext(query, DEFAULT_TOP_K);
    }

    public List<CodeChunk> retrieveRelevantContext(String query, int topK) {
        float[] queryEmbedding = embeddingClient.embed(query);
        return vectorStore.search(queryEmbedding, topK);
    }
}