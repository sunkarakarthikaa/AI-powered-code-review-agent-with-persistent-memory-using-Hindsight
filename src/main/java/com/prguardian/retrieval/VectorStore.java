package com.prguardian.retrieval;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory vector index: a list of CodeChunks, searched by linear scan
 * cosine similarity.
 *
 * Deliberate scale decision: for a bounded demo repo (tens to hundreds of
 * files), a linear scan is O(n) and takes single-digit milliseconds — a
 * dedicated vector database (pgvector, Pinecone, Milvus) only starts paying
 * for itself at much larger scale, where you need approximate nearest-
 * neighbor indexes (HNSW, IVF) to stay fast. Naming that trade-off out loud
 * is the point — not pretending this is production-scale infrastructure.
 *
 * CopyOnWriteArrayList because indexing happens rarely (once at startup)
 * but searches happen often (every PR review) — optimizing reads over writes.
 */
@Component
public class VectorStore {

    private final List<CodeChunk> chunks = new CopyOnWriteArrayList<>();

    public void add(CodeChunk chunk) {
        chunks.add(chunk);
    }

    public int size() {
        return chunks.size();
    }

    /**
     * Returns the top-K most similar chunks to the query embedding,
     * ranked by cosine similarity, highest first.
     */
    public List<CodeChunk> search(float[] queryEmbedding, int topK) {
        List<ScoredChunk> scored = new ArrayList<>();
        for (CodeChunk chunk : chunks) {
            double score = CosineSimilarity.compute(queryEmbedding, chunk.getEmbedding());
            scored.add(new ScoredChunk(chunk, score));
        }

        scored.sort(Comparator.comparingDouble((ScoredChunk sc) -> sc.score).reversed());

        return scored.stream()
                .limit(topK)
                .map(sc -> sc.chunk)
                .toList();
    }

    private record ScoredChunk(CodeChunk chunk, double score) {
    }
}