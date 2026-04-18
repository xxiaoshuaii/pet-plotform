package com.pethub.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeIngestUtil {

    private static final String KNOWLEDGE_PATH = "classpath*:kb/*.txt";

    private final VectorStore vectorStore;

    @PostConstruct
    public void init() {
        try {
            List<Document> docs = loadKnowledgeDocuments();
            if (docs.isEmpty()) {
                log.info("No knowledge documents found under {}", KNOWLEDGE_PATH);
                return;
            }
            vectorStore.add(docs);
            log.info("Loaded {} knowledge chunks into vector store", docs.size());
        }
        catch (Exception e) {
            // Do not block the whole app startup when embedding/vector ingestion is unavailable.
            log.warn("Knowledge ingestion skipped because vector initialization failed: {}", e.getMessage(), e);
        }
    }

    private List<Document> loadKnowledgeDocuments() throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(KNOWLEDGE_PATH);
        List<Document> docs = new ArrayList<>();
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(300)
                .withMinChunkSizeChars(80)
                .withMinChunkLengthToEmbed(20)
                .withMaxNumChunks(50)
                .build();

        for (Resource resource : resources) {
            if (!resource.exists()) {
                continue;
            }

            String content = resource.getContentAsString(StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                continue;
            }

            String filename = resource.getFilename() == null ? "unknown.txt" : resource.getFilename();
            Document sourceDocument = new Document(content, buildMetadata(filename));
            docs.addAll(splitter.apply(List.of(sourceDocument)));
        }

        return docs;
    }

    private Map<String, Object> buildMetadata(String filename) {
        if ("platform_rules.txt".equals(filename)) {
            return Map.of("type", "rule", "source", filename);
        }
        if ("commonresult.txt".equals(filename)) {
            return Map.of("type", "result", "source", filename);
        }
        return Map.of("type", "knowledge", "source", filename);
    }
}
