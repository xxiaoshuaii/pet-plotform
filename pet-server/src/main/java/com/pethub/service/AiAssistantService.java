package com.pethub.service;

import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiChatMessageVO;
import com.pethub.pojo.vo.AiChatSessionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiAssistantService {

    void consultStream(AiConsultDTO aiConsultDTO, SseEmitter emitter, HttpServletResponse response);

    List<AiChatSessionVO> listSessions(Long userId);

    List<AiChatMessageVO> listMessages(Long userId, Long sessionId);

    void deleteSession(Long userId, Long sessionId);
}
