package gcy.ai.config;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(20)
                .build();
    }

    @Bean
    @ConditionalOnBean(RedisEmbeddingStore.class)
    public EmbeddingStore embeddingStore(RedisEmbeddingStore redisEmbeddingStore) {
        log.info("Redis RediSearch 可用，初始化 RAG 向量知识库...");
        List<Document> documents = ClassPathDocumentLoader
                .loadDocuments("content");
        DocumentSplitter splitter = DocumentSplitters
                .recursive(500, 100);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)
                .documentSplitter(splitter)
                .build();
        ingestor.ingest(documents);
        return redisEmbeddingStore;
    }

    @Bean
    @ConditionalOnBean(EmbeddingStore.class)
    public ContentRetriever contentRetriever(EmbeddingStore embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .minScore(0.5)
                .maxResults(3)
                .build();
    }

    /**
     * 兜底：当 RediSearch 不可用时，提供空的 ContentRetriever
     */
    @Bean
    @ConditionalOnMissingBean(ContentRetriever.class)
    public ContentRetriever emptyContentRetriever() {
        log.warn("Redis RediSearch 不可用，AI 知识库检索功能已禁用");
        return query -> Collections.emptyList();
    }
}
