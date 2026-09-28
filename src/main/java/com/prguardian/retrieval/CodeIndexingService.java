package com.prguardian.retrieval;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Loads every file under resources/sample-repo, embeds it, and adds it to
 * the VectorStore. Runs once at application startup via CommandLineRunner.
 *
 * In a real system this would instead be triggered by a webhook when the
 * repo changes, or a scheduled re-index job — but "index a fixed sample
 * repo on boot" is the right scope for demonstrating the mechanism end to
 * end without building a GitHub-sync pipeline.
 */
@Component
public class CodeIndexingService implements CommandLineRunner {

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    public CodeIndexingService(EmbeddingClient embeddingClient, VectorStore vectorStore) {
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        indexSampleRepo();
    }

    public void indexSampleRepo() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:sample-repo/*");

        for (Resource resource : resources) {
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String filePath = resource.getFilename();

            float[] embedding = embeddingClient.embed(content);
            CodeChunk chunk = new CodeChunk(UUID.randomUUID().toString(), filePath, content, embedding);
            vectorStore.add(chunk);
        }

        System.out.println("[CodeIndexingService] Indexed " + vectorStore.size() + " chunks from sample-repo");
    }
}