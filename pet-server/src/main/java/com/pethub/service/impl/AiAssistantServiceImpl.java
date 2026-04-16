package com.pethub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethub.common.exception.BusinessException;
import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiConsultVO;
import com.pethub.properties.QwenProperties;
import com.pethub.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final QwenProperties qwenProperties;
    private final ObjectMapper objectMapper;

//    @Override
//    public AiConsultVO consult(AiConsultDTO aiConsultDTO) {
//        validate(aiConsultDTO);
//
//        try {
//            // 创建 HTTP 请求
//            HttpClient client = HttpClient.newHttpClient();
//            // 创建 HTTP 请求体
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(qwenProperties.getBaseUrl() + "/chat/completions"))
//                    .timeout(Duration.ofSeconds(60))
//                    .header("Authorization", "Bearer " + qwenProperties.getApiKey())
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(aiConsultDTO.getMessage(), false)))
//                    .build();
//            //获得响应结果
//            //阅读整段对话，不开启流式
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
//            //解析响应结果，因为该结果是一个JSON树
//            JsonNode root = objectMapper.readTree(response.body());
//            //由于千问回答格式是一颗JSON树，需要层层剖析，获得content内容
//            String content = root.path("choices").path(0).path("message").path("content").asText();
//            if (content == null || content.isBlank()) {
//                throw new BusinessException("千问返回为空");
//            }
//            //返回结果
//            return new AiConsultVO(content, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "qwen");
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new BusinessException("千问调用失败：" + e.getMessage());
//        }
//    }
//
    @Override
    public SseEmitter consultStream(AiConsultDTO aiConsultDTO) {
        validate(aiConsultDTO);

        //创建 SSEEmitter，向浏览器推送
        SseEmitter emitter = new SseEmitter(0L);
        //异步执行
        CompletableFuture.runAsync(() -> streamQwen(aiConsultDTO.getMessage(), emitter));
        return emitter;
    }

    //流式调用千问
    private void streamQwen(String message, SseEmitter emitter) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qwenProperties.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Authorization", "Bearer " + qwenProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(message, true)))
                    .build();

            //获得Stream流（ofLines）
            HttpResponse<java.util.stream.Stream<String>> response =
                    client.send(request, HttpResponse.BodyHandlers.ofLines());
            //解析流
            parseAndEmit(response.body(), emitter);
            //解析到done结束
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("千问调用失败：" + e.getMessage()));
            } catch (Exception ignored) {
            }
            emitter.completeWithError(e);
        }
    }

    private void parseAndEmit(java.util.stream.Stream<String> lines, SseEmitter emitter) throws Exception {
        for (String line : (Iterable<String>) lines::iterator) {
            line = line.trim();
            if (!line.startsWith("data:")) {
                continue;
            }

            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
                break;
            }

            JsonNode json = objectMapper.readTree(data);
            String chunk = json.path("choices").path(0).path("delta").path("content").asText();
            if (chunk != null && !chunk.isBlank()) {
                emitter.send(SseEmitter.event().name("message").data(chunk));
            }
        }
    }

    private String buildRequestBody(String message, boolean stream) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "model", qwenProperties.getModel(),
                    "stream", stream,
                    "temperature", qwenProperties.getTemperature(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是 PetHub 的宠物咨询助手，请直接用中文回答用户问题。"),
                            Map.of("role", "user", "content", message)
                    )
            ));
        } catch (Exception e) {
            throw new BusinessException("构造千问请求失败");
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
