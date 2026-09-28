package com.prguardian.retrieval;

/**
 * Abstraction over "turn text into a vector." Kept as an interface so the
 * embedding provider is swappable (OpenAI, Voyage AI, a local sentence-
 * transformers model, etc.) without touching VectorStore, indexing, or
 * retrieval logic. This is the same reasoning as coding to an interface
 * anywhere else — the AI-provider dependency shouldn't leak into business
 * logic.
 */
public interface EmbeddingClient {

    float[] embed(String text);
}