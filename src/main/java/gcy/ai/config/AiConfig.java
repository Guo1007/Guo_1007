package gcy.ai.config;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private static final String EMBEDDING_INGESTED_KEY = "ai:embedding:ingested";

    private static final Duration EMBEDDING_TTL = Duration.ofDays(30);

    private final ChatMemoryStore redisChatMemoryStore;

    private final RedisEmbeddingStore redisEmbeddingStore;

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        Boolean alreadyIngested = stringRedisTemplate.hasKey(EMBEDDING_INGESTED_KEY);
        if (alreadyIngested) {
            log.info("知识库向量已存在，跳过 Embedding 摄入");
            return redisEmbeddingStore;
        }
        log.info("开始摄入知识库文档到向量存储...");
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)
                .documentSplitter(splitter)
                .build();
        ingestor.ingest(documents);
        Set<String> embeddingKeys = stringRedisTemplate.keys("embedding:*");
        if (!embeddingKeys.isEmpty()) {
            for (String key : embeddingKeys) {
                stringRedisTemplate.expire(key, EMBEDDING_TTL);
            }
            log.info("已为 {} 个向量 key 设置过期时间 {} 天", embeddingKeys.size(), EMBEDDING_TTL.toDays());
        }
        stringRedisTemplate.opsForValue()
                .set(EMBEDDING_INGESTED_KEY, String.valueOf(System.currentTimeMillis()), EMBEDDING_TTL);
        log.info("知识库文档摄入完成，向量数据有效期 {} 天", EMBEDDING_TTL.toDays());
        return redisEmbeddingStore;
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .minScore(0.5)
                .maxResults(3)
                .build();
    }
}
