package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.AiConsultDTO;
import com.pethub.pojo.vo.AiChatMessageVO;
import com.pethub.pojo.vo.AiChatSessionVO;
import com.pethub.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserAiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping(value = "/consult/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> consultStream(@RequestBody AiConsultDTO aiConsultDTO) {
        Flux<String> stream = aiAssistantService.consultStream(aiConsultDTO);
        return ResponseEntity.ok()
                .header("X-AI-Session-Id", String.valueOf(aiConsultDTO.getSessionId()))
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }

    @GetMapping("/sessions")
    public Result<List<AiChatSessionVO>> listSessions() {
        return Result.success(aiAssistantService.listSessions(BaseContext.getCurrentId()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiChatMessageVO>> listMessages(@PathVariable Long sessionId) {
        return Result.success(aiAssistantService.listMessages(BaseContext.getCurrentId(), sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        aiAssistantService.deleteSession(BaseContext.getCurrentId(), sessionId);
        return Result.success();
    }
}
