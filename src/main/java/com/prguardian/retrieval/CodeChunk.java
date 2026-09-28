package com.prguardian.retrieval;

/**
 * A single unit of retrievable context: one file (or a slice of one),
 * plus its embedding vector. This is what we search over.
 *
 * Chunking strategy here is intentionally simple: one chunk per file.
 * For a real large repo you'd split big files into smaller windows
 * (e.g. per-function or fixed line-count blocks) so retrieval is more
 * precise — but for a bounded demo repo, whole-file chunks keep the
 * example easy to reason about and explain.
 */
public class CodeChunk {

    private final String id;
    private final String filePath;
    private final String content;
    private final float[] embedding;

    public CodeChunk(String id, String filePath, String content, float[] embedding) {
        this.id = id;
        this.filePath = filePath;
        this.content = content;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getContent() {
        return content;
    }

    public float[] getEmbedding() {
        return embedding;
    }
}