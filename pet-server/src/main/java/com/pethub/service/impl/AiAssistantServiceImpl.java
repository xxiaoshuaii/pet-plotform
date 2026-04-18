package com.pethub.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethub.common.context.BaseContext;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.AiChatMessageMapper;
import com.pethub.mapper.AiChatSessionMapper;
import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.entity.AiChatMessage;
import com.pethub.pojo.entity.AiChatSession;
import com.pethub.pojo.vo.AiChatMessageVO;
import com.pethub.pojo.vo.AiChatSessionVO;
import com.pethub.properties.QwenProperties;
import com.pethub.service.AiAssistantService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            你是 PetHub 的宠物咨询助手，请直接用中文回答用户问题。
            回答要专业、温和、易懂。
            如果信息不足，先说明限制，再给出可执行建议。
            如果用户提供了图片链接，也请结合图片信息一起分析。
            """;

    private static final int MAX_SESSION_TITLE_LENGTH = 12;

    private final ChatClient.Builder chatClientBuilder;
    private final QwenProperties qwenProperties;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final ObjectMapper objectMapper;
    private final SpringAiChatMemory chatMemory;

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

            //为什么这里要设置响应头,响应给谁
            response.setHeader("X-AI-Session-Id", String.valueOf(session.getId()));
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");

            chatMemory.prepareUserMessage(conversationId, aiConsultDTO.getImages());

            Flux<String> stream = chatClientBuilder
                    .build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(appendImagesToMessage(userMessage, aiConsultDTO.getImages()))
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(conversationId)
                            .build())
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

    private String appendImagesToMessage(String content, List<String> images) {
        if (images == null || images.isEmpty()) {
            return content;
        }
        return content + "\n\n图片链接: " + String.join("，", images);
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
    }
}
