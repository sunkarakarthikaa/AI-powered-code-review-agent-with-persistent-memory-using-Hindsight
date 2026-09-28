package com.prguardian.retrieval;

/**
 * Cosine similarity: measures the angle between two vectors, not their
 * magnitude. That's the right metric for embeddings — we care whether two
 * pieces of text mean similar things, not how "long" their vectors are.
 * Returns a value from -1 (opposite) to 1 (identical direction).
 */
public final class CosineSimilarity {

    private CosineSimilarity() {
    }

    public static double compute(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must be the same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}