package com.pethub.controller.user;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiConsultVO;
import com.pethub.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserAiController {

    private final AiAssistantService aiAssistantService;

//    @PostMapping("/consult")
//    public Result<AiConsultVO> consult(@RequestBody AiConsultDTO aiConsultDTO) {
//        return Result.success(aiAssistantService.consult(aiConsultDTO));
//    }

    @PostMapping(value = "/consult/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultStream(@RequestBody AiConsultDTO aiConsultDTO) {
        return aiAssistantService.consultStream(aiConsultDTO);
    }
}
