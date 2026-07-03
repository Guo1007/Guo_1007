package gcy.ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gcy.ai.aiservice.FurnitureAiService;
import gcy.system.entity.dto.UserDTO;
import gcy.system.utils.UserHolder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static gcy.system.utils.RedisConstants.AI_CHAT_MEMORY_KEY;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final FurnitureAiService furnitureAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/chat/stream", produces = "text/html;charset=utf-8")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return Flux.just(formatSse(errorJson("消息内容不能为空")));
        }
        UserDTO user = UserHolder.getUser();
        Long userId = (user != null) ? user.getId() : 0L;
        String conversationId = request.getConversationId();
        boolean isNew = false;
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
            isNew = true;
        }
        String memoryId = AI_CHAT_MEMORY_KEY + userId + ":" + conversationId;
        log.debug("AI chat: userId={}, conversationId={}, isNew={}, message={}",
                userId, conversationId, isNew, message);
        Flux<String> metaEvent = isNew
                ? Flux.just(formatSse(metaJson(conversationId)))
                : Flux.empty();
        Flux<String> chatStream = furnitureAiService.streamChat(memoryId, message)
                .map(chunk -> formatSse(contentJson(chunk)))
                .concatWith(Flux.just("data: [DONE]\n\n"))
                .onErrorResume(e -> {
                    log.error("AI聊天流式调用失败: {}", e.getMessage(), e);
                    return Flux.just(formatSse(errorJson("AI客服暂时无法响应，请稍后再试")));
                });

        return Flux.concat(metaEvent, chatStream);
    }

    private String formatSse(String data) {
        return "data: " + data + "\n\n";
    }

    private String metaJson(String conversationId) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "meta");
            node.put("conversationId", conversationId);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"meta\"}";
        }
    }

    private String contentJson(String content) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("content", content);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{\"content\":\"\"}";
        }
    }

    private String errorJson(String errorMsg) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("error", errorMsg);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"序列化异常\"}";
        }
    }

    @Data
    public static class ChatRequest {
        private String message;
        private String conversationId;
    }

}
