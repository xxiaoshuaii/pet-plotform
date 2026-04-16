package com.pethub.service;

import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiChatMessageVO;
import com.pethub.pojo.vo.AiChatSessionVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiAssistantService {

    Flux<String> consultStream(AiConsultDTO aiConsultDTO);

    List<AiChatSessionVO> listSessions(Long userId);

    List<AiChatMessageVO> listMessages(Long userId, Long sessionId);

    void deleteSession(Long userId, Long sessionId);
}
