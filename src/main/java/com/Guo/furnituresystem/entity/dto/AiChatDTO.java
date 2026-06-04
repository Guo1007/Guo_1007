package com.Guo.furnituresystem.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatDTO {

    /**
     * 用户当前消息
     */
    private String message;

    /**
     * 会话ID（用于保持上下文）
     */
    private String conversationId;

    /**
     * 历史消息（可选，用于传递上下文）
     */
    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;  // "user" 或 "assistant"
        private String content;
    }
}