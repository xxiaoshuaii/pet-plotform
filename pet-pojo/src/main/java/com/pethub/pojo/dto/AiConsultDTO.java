package com.pethub.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message;

    private Long sessionId;

    private List<String> images = new ArrayList<>();

    private List<ChatMessageDTO> history = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String role;

        private String content;

        private List<String> images = new ArrayList<>();
    }
}
