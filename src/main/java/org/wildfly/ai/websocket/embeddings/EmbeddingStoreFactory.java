/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket.embeddings;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import io.weaviate.client.v1.schema.model.Property;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class EmbeddingStoreFactory {

    public static EmbeddingStore<TextSegment> createEmbeddingStore(List<Document> documents, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(1500, 500))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        return embeddingStore;
    }

    public static EmbeddingStoreContentRetriever createWeaviateEmbeddingStoreContentRetriever(String host, int port, List<String> metadata) {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = WeaviateEmbeddingStore.builder()
            .scheme("http")
            .host(host)
            .port(port)
            .objectClass("MyGreatClass")
            .metadataKeys(metadata)
            .avoidDups(true)
            .consistencyLevel("ALL")
            .build();
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.7) // we want to retrieve segments at least somewhat similar to user query
                .build();
    }

    public static EmbeddingStore<TextSegment> loadEmbeddingStore(Path filePath) {
        return InMemoryEmbeddingStore.fromFile(filePath);
    }

    public static EmbeddingStoreContentRetriever createEmbeddingStoreContentRetriever(String filePath) {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        Path file = new File(filePath).toPath();
        EmbeddingStore<TextSegment> store = loadEmbeddingStore(file);
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(4) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.7) // we want to retrieve segments at least somewhat similar to user query
                .build();
    }
    
}
