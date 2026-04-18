package com.pethub.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.AiChatMessageMapper;
import com.pethub.mapper.AiChatSessionMapper;
import com.pethub.pojo.entity.AiChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SpringAiChatMemory implements ChatMemory {

    private static final int MAX_HISTORY_MESSAGES = 12;

    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final ObjectMapper objectMapper;

    private final Map<String, List<String>> pendingUserImages = new ConcurrentHashMap<>();

    public void prepareUserMessage(String conversationId, List<String> images) {
        pendingUserImages.put(conversationId, images == null ? Collections.emptyList() : List.copyOf(images));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }

        Long sessionId = parseConversationId(conversationId);
        for (Message message : messages) {
            saveMessage(sessionId, message);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        Long sessionId = parseConversationId(conversationId);
        return aiChatMessageMapper.selectRecentBySessionId(sessionId, MAX_HISTORY_MESSAGES)
                .stream()
                .map(this::toMessage)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        Long sessionId = parseConversationId(conversationId);
        aiChatMessageMapper.softDeleteBySessionId(sessionId, LocalDateTime.now());
    }

    private void saveMessage(Long sessionId, Message message) {
        if (message == null || message.getText() == null || message.getText().isBlank()) {
            return;
        }

        String role = resolveRole(message);
        if (role == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AiChatMessage entity = new AiChatMessage();
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(message.getText().trim());
        entity.setImages(writeImages("user".equals(role)
                ? pendingUserImages.remove(String.valueOf(sessionId))
                : Collections.emptyList()));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        aiChatMessageMapper.insert(entity);
        aiChatSessionMapper.touchSessionActivity(sessionId, now, now);
    }

    private Message toMessage(AiChatMessage historyMessage) {
        if (historyMessage == null || historyMessage.getContent() == null || historyMessage.getContent().isBlank()) {
            return null;
        }

        String content = historyMessage.getContent().trim();
        List<String> images = parseImages(historyMessage.getImages());
        if ("user".equalsIgnoreCase(historyMessage.getRole()) && !images.isEmpty()) {
            content = content + "\n\n图片链接: " + String.join("，", images);
        }

        if ("assistant".equalsIgnoreCase(historyMessage.getRole())) {
            return new AssistantMessage(content);
        }
        if ("system".equalsIgnoreCase(historyMessage.getRole())) {
            return new SystemMessage(content);
        }
        return new UserMessage(content);
    }

    private String resolveRole(Message message) {
        if (message instanceof AssistantMessage) {
            return "assistant";
        }
        if (message instanceof SystemMessage) {
            return "system";
        }
        if (message instanceof UserMessage) {
            return "user";
        }
        return null;
    }

    private Long parseConversationId(String conversationId) {
        try {
            return Long.valueOf(conversationId);
        }
        catch (NumberFormatException e) {
            throw new BusinessException("会话标识无效");
        }
    }

    private String writeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? Collections.emptyList() : images);
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
