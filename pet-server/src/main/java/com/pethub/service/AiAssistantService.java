package com.pethub.service;

import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiConsultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiAssistantService {

//    AiConsultVO consult(AiConsultDTO aiConsultDTO);

    SseEmitter consultStream(AiConsultDTO aiConsultDTO);
}
