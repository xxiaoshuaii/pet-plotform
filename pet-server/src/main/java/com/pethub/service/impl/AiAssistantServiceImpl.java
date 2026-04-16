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
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SYSTEM_PROMPT = "你是 PetHub 的宠物咨询助手，请直接用中文回答用户问题。"
            + " 回答要专业、温和、易懂。"
            + " 如果信息不足，先说明限制，再给出可执行建议。"
            + " 如果用户提供了图片链接，也请结合图片信息一起分析。";

    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_SESSION_TITLE_LENGTH = 12;

    private final ChatClient.Builder chatClientBuilder;
    private final QwenProperties qwenProperties;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<String> consultStream(AiConsultDTO aiConsultDTO) {
        validate(aiConsultDTO);

        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("请先登录后再使用 AI 助手");
        }

        AiChatSession session = resolveSession(userId, aiConsultDTO);
        saveUserMessage(session, aiConsultDTO.getMessage().trim(), aiConsultDTO.getImages());

        List<Message> messages = buildMessages(session.getId());
        Prompt prompt = new Prompt(messages);
        StringBuilder assistantContent = new StringBuilder();
        AtomicBoolean saved = new AtomicBoolean(false);

        return chatClientBuilder
                .build()
                .prompt(prompt)
                .stream()
                .content()
                .doOnNext(assistantContent::append)
                .doOnComplete(() -> saveAssistantMessageIfNecessary(session, assistantContent, saved))
                .doOnError(error -> saveAssistantMessageIfNecessary(session, assistantContent, saved));
    }

    @Override
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
        aiChatMessageMapper.softDeleteBySessionId(sessionId, now);
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

    private void saveUserMessage(AiChatSession session, String content, List<String> images) {
        LocalDateTime now = LocalDateTime.now();
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setRole("user");
        message.setContent(content);
        message.setImages(writeImages(images));
        message.setCreateTime(now);
        message.setUpdateTime(now);
        aiChatMessageMapper.insert(message);
        aiChatSessionMapper.touchSession(session.getId(), session.getTitle(), now, now);
    }

    private void saveAssistantMessageIfNecessary(AiChatSession session, StringBuilder assistantContent, AtomicBoolean saved) {
        if (!saved.compareAndSet(false, true)) {
            return;
        }
        String content = assistantContent.toString().trim();
        if (content.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setRole("assistant");
        message.setContent(content);
        message.setImages(writeImages(Collections.emptyList()));
        message.setCreateTime(now);
        message.setUpdateTime(now);
        aiChatMessageMapper.insert(message);
        aiChatSessionMapper.touchSession(session.getId(), session.getTitle(), now, now);
    }

    private List<Message> buildMessages(Long sessionId) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));

        List<AiChatMessage> history = aiChatMessageMapper.selectRecentBySessionId(sessionId, MAX_HISTORY_MESSAGES);
        for (AiChatMessage historyMessage : history) {
            Message message = toMessage(historyMessage);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    private Message toMessage(AiChatMessage historyMessage) {
        if (historyMessage == null || historyMessage.getContent() == null || historyMessage.getContent().isBlank()) {
            return null;
        }

        String role = historyMessage.getRole();
        String content = historyMessage.getContent().trim();
        List<String> images = parseImages(historyMessage.getImages());
        if ("user".equalsIgnoreCase(role) && !images.isEmpty()) {
            content = content + "\n\n图片链接：" + String.join("，", images);
        }

        if ("assistant".equalsIgnoreCase(role)) {
            return new AssistantMessage(content);
        }
        if ("system".equalsIgnoreCase(role)) {
            return new SystemMessage(content);
        }
        return new UserMessage(content);
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

    private String writeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? Collections.emptyList() : images);
        } catch (Exception e) {
            throw new BusinessException("保存图片信息失败");
        }
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<>() {
            });
        } catch (Exception e) {
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
