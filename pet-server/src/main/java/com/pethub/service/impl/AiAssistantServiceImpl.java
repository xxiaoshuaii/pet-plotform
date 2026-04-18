package com.pethub.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethub.common.context.BaseContext;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.AiChatMessageMapper;
import com.pethub.mapper.AiChatSessionMapper;
import com.pethub.mapper.CategoryMapper;
import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.entity.AiChatMessage;
import com.pethub.pojo.entity.AiChatSession;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.AiChatMessageVO;
import com.pethub.pojo.vo.AiChatSessionVO;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.properties.QwenProperties;
import com.pethub.service.AiAssistantService;
import com.pethub.utils.KnowledgeIngestUtil;
import com.pethub.utils.PetUtill;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.MediaTypeFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            你是 PetHub 的宠物咨询助手，请直接用中文回答用户问题。
            你同时熟悉平台规则、宠物信息发布流程、宠物浏览与交易辅助场景。
            回答要专业、温和、易懂，优先直接回答用户真正想问的问题，不要一上来只做免责声明。
            请优先依据检索到的知识库内容回答。
            当用户询问“你们这里有没有猫/狗/某类宠物”“能不能买到”“能不能发布”这类问题时，
            优先理解为平台是否支持该类宠物的信息发布、浏览、咨询或交易辅助，不要误解为平台自己提供线下实体宠物、寄养、住院或领养服务。
            这类问题如果可以通过工具查询平台当前宠物数据，请优先调用工具查询，再根据工具结果作答，不要跳过工具直接凭空回答。
            只有当用户明确问到医疗、疫苗、寄养、住院、线下收猫卖猫等实体能力时，再说明平台不提供该类线下服务。
            如果信息不足，先说明限制，再给出可执行建议。
            如果涉及平台规则，先明确规则结论，再补充原因或建议。
            如果可以直接回答，就不要使用 Markdown 粗体符号，也不要堆砌客套话。
            如果用户提供了图片链接，也请结合图片信息一起分析。
            """;

    private static final int MAX_SESSION_TITLE_LENGTH = 12;
    private static final List<String> AVAILABILITY_INTENT_TERMS = List.of(
            "有没有", "有吗", "有嘛", "有么", "能不能", "能买吗", "可以买", "想买", "购买",
            "出售", "卖吗", "在售", "上架", "能发布", "可发布", "发布", "想养", "想看", "推荐"
    );
    private static final Map<String, String> PET_KEYWORD_ALIASES = Map.of(
            "猫咪", "猫",
            "喵", "猫",
            "喵咪", "猫",
            "狗狗", "狗",
            "汪", "狗",
            "汪星人", "狗",
            "犬", "狗",
            "金鱼", "水中动物"
    );

    private final ChatClient.Builder chatClientBuilder;
    private final QwenProperties qwenProperties;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;
    private final SpringAiChatMemory chatMemory;
    private final VectorStore vectorStore;
    private final PetUtill petUtill;

    @Override
    public void consultStream(AiConsultDTO aiConsultDTO, SseEmitter emitter, HttpServletResponse response) {
        try {
            validate(aiConsultDTO);

            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                emitter.completeWithError(new BusinessException("请先登录后再使用 AI 助手"));
                return;
            }

            AiChatSession session = resolveSession(userId, aiConsultDTO);
            String conversationId = String.valueOf(session.getId());
            String userMessage = aiConsultDTO.getMessage().trim();
            String prefetchedContext = resolvePrefetchedContext(userMessage);

            //为什么这里要设置响应头,响应给谁
            response.setHeader("X-AI-Session-Id", String.valueOf(session.getId()));
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");

            chatMemory.prepareUserMessage(conversationId, aiConsultDTO.getImages());

            QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder()
                            .similarityThreshold(0.75d)
                            .topK(4)
                            .build())
                    .build();

            Flux<String> stream = chatClientBuilder
                    .build()
                    .prompt(buildPrompt(userMessage, aiConsultDTO.getImages(), prefetchedContext))
                    .tools(petUtill)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(conversationId)
                            .build(),
                            qaAdvisor)
                    .stream()
                    .content();

            //chunk代表什么
            stream.subscribe(
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().data(chunk));
                        }
                        catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    emitter::completeWithError,
                    emitter::complete
            );
        }
        catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    @Override
    //sql语句的理解
    public List<AiChatSessionVO> listSessions(Long userId) {
        return aiChatSessionMapper.selectListByUserId(userId);
    }

    @Override
    public List<AiChatMessageVO> listMessages(Long userId, Long sessionId) {
        AiChatSession session = aiChatSessionMapper.selectByIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        List<AiChatMessage> messages = aiChatMessageMapper.selectListBySessionId(sessionId);
        List<AiChatMessageVO> result = new ArrayList<>(messages.size());
        for (AiChatMessage message : messages) {
            result.add(new AiChatMessageVO(
                    message.getId(),
                    message.getSessionId(),
                    message.getRole(),
                    message.getContent(),
                    parseImages(message.getImages()),
                    message.getCreateTime()
            ));
        }
        return result;
    }

    @Override
    public void deleteSession(Long userId, Long sessionId) {
        AiChatSession session = aiChatSessionMapper.selectByIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        chatMemory.clear(String.valueOf(sessionId));
        aiChatSessionMapper.softDeleteByIdAndUserId(sessionId, userId, now);
    }

    private AiChatSession resolveSession(Long userId, AiConsultDTO aiConsultDTO) {
        if (aiConsultDTO.getSessionId() != null) {
            AiChatSession session = aiChatSessionMapper.selectByIdAndUserId(aiConsultDTO.getSessionId(), userId);
            if (session == null) {
                throw new BusinessException("会话不存在或无权访问");
            }
            return session;
        }

        LocalDateTime now = LocalDateTime.now();
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(buildSessionTitle(aiConsultDTO.getMessage(), aiConsultDTO.getImages()));
        session.setLastMessageTime(now);
        session.setCreateTime(now);
        session.setUpdateTime(now);
        aiChatSessionMapper.insert(session);
        aiConsultDTO.setSessionId(session.getId());
        return session;
    }

    private String buildSessionTitle(String content, List<String> images) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            normalized = images != null && !images.isEmpty() ? "图片分析" : "新会话";
        }
        return normalized.length() > MAX_SESSION_TITLE_LENGTH
                ? normalized.substring(0, MAX_SESSION_TITLE_LENGTH)
                : normalized;
    }

    private Prompt buildPrompt(String content, List<String> images, String prefetchedContext) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        messages.add(buildUserMessage(buildUserPrompt(content, prefetchedContext), images));
        return new Prompt(messages, buildChatOptions(images));
    }

    private UserMessage buildUserMessage(String content, List<String> images) {
        List<Media> mediaList = buildMediaList(images);
        if (mediaList.isEmpty()) {
            return new UserMessage(content);
        }
        return UserMessage.builder()
                .text(content)
                .media(mediaList)
                .build();
    }

    private List<Media> buildMediaList(List<String> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(this::buildImageMedia)
                .toList();
    }

    private Media buildImageMedia(String imageUrl) {
        MimeType mimeType = MediaTypeFactory.getMediaType(imageUrl)
                .orElse(org.springframework.http.MediaType.IMAGE_JPEG);
        return new Media(mimeType, URI.create(imageUrl));
    }

    private OpenAiChatOptions buildChatOptions(List<String> images) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder()
                .model(resolveChatModel(images));
        if (qwenProperties.getTemperature() != null) {
            builder.temperature(qwenProperties.getTemperature());
        }
        return builder.build();
    }

    private String resolveChatModel(List<String> images) {
        if (images == null || images.isEmpty()) {
            return qwenProperties.getModel();
        }
        return StringUtils.hasText(qwenProperties.getVisionModel())
                ? qwenProperties.getVisionModel()
                : qwenProperties.getModel();
    }

    private String buildUserPrompt(String content, String prefetchedContext) {
        String message = content == null ? "" : content.trim();
        if (prefetchedContext == null || prefetchedContext.isBlank()) {
            return message;
        }
        return """
                用户原问题：
                %s

                后端预查询结果：
                %s

                请优先基于后端预查询结果回答；如果查询结果为空，就明确告诉用户当前未查到对应信息。
                """.formatted(message, prefetchedContext);
    }

    private String resolvePrefetchedContext(String userMessage) {
        String keyword = extractPetAvailabilityKeyword(userMessage);
        if (keyword == null) {
            return "";
        }
        return petUtill.searchAvailablePets(keyword);
    }

    private String extractPetAvailabilityKeyword(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return null;
        }
        String normalized = userMessage.trim().toLowerCase(Locale.ROOT);
        String categoryKeyword = matchCategoryKeyword(normalized);
        if (categoryKeyword == null) {
            return null;
        }
        if (!isAvailabilityIntent(normalized, categoryKeyword)) {
            return null;
        }
        return categoryKeyword;
    }

    private boolean isAvailabilityIntent(String normalized, String categoryKeyword) {
        if (normalized.endsWith("吗") || normalized.endsWith("嘛") || normalized.endsWith("么") || normalized.endsWith("呢")) {
            return true;
        }
        if (normalized.length() <= categoryKeyword.length() + 4) {
            return true;
        }
        return AVAILABILITY_INTENT_TERMS.stream().anyMatch(normalized::contains);
    }

    private String matchCategoryKeyword(String normalized) {
        List<CategoryVO> enabledCategories = categoryMapper.selectPage(new CategoryQuery(null, 1, null, null));
        String categoryMatch = enabledCategories.stream()
                .map(CategoryVO::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(name -> normalized.contains(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
        if (categoryMatch != null) {
            return categoryMatch;
        }
        return PET_KEYWORD_ALIASES.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void validate(AiConsultDTO aiConsultDTO) {
        if (aiConsultDTO == null || aiConsultDTO.getMessage() == null || aiConsultDTO.getMessage().isBlank()) {
            throw new BusinessException("请输入咨询内容");
        }
        if (!qwenProperties.isEnabled()) {
            throw new BusinessException("千问未启用");
        }
        if (qwenProperties.getApiKey() == null || qwenProperties.getApiKey().isBlank()) {
            throw new BusinessException("请先配置千问 API Key");
        }
        if (qwenProperties.getModel() == null || qwenProperties.getModel().isBlank()) {
            throw new BusinessException("请先配置千问模型");
        }
        if (aiConsultDTO.getImages() != null && !aiConsultDTO.getImages().isEmpty()
                && (qwenProperties.getVisionModel() == null || qwenProperties.getVisionModel().isBlank())
                && (qwenProperties.getModel() == null || qwenProperties.getModel().isBlank())) {
            throw new BusinessException("请先配置可识别图片的视觉模型");
        }
    }
}
