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

/**
 * AI聊天控制器，处理用户与AI助手的对话交互。
 * <p>
 * 提供基于SSE（Server-Sent Events）的流式聊天接口，支持多轮对话上下文管理。
 * 每次对话通过 conversationId 标识，首次请求自动生成会话ID并通过元事件返回给客户端。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final FurnitureAiService furnitureAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI流式聊天接口，以SSE格式实时推送AI回复内容。
     * <p>
     * 客户端发送用户消息后，服务端通过SSE流逐步返回AI生成的回复文本。
     * 若请求中未携带 conversationId，则自动创建新会话并通过 meta 事件返回会话ID；
     * 若携带已有 conversationId，则基于历史对话上下文继续交互。
     * </p>
     *
     * @param request 聊天请求体，包含用户消息内容和可选的会话ID
     * @return SSE格式的响应流，包含meta事件（新会话时）、content事件（AI回复片段）、
     *         [DONE]结束标记以及异常时的error事件
     */
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

    /**
     * 将JSON数据格式化为SSE（Server-Sent Events）标准格式。
     *
     * @param data 待发送的JSON字符串数据
     * @return 符合SSE规范的格式化字符串，格式为 "data: {data}\n\n"
     */
    private String formatSse(String data) {
        return "data: " + data + "\n\n";
    }

    /**
     * 构建元信息JSON，用于在新建会话时通知客户端会话ID。
     *
     * @param conversationId 新生成的会话唯一标识
     * @return 包含 type 为 "meta" 和 conversationId 的JSON字符串
     */
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

    /**
     * 构建AI回复内容的JSON，用于封装单个流式文本片段。
     *
     * @param content AI生成的文本片段内容
     * @return 包含 content 字段的JSON字符串
     */
    private String contentJson(String content) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("content", content);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{\"content\":\"\"}";
        }
    }

    /**
     * 构建错误信息JSON，用于向前端返回异常提示。
     *
     * @param errorMsg 错误描述信息
     * @return 包含 error 字段的JSON字符串
     */
    private String errorJson(String errorMsg) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("error", errorMsg);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"序列化异常\"}";
        }
    }

    /**
     * AI聊天请求数据传输对象，封装前端发送的聊天请求参数。
     *
     * @author 郭名城
     * @date 2026-07-30
     */
    @Data
    public static class ChatRequest {
        /** 用户输入的消息内容 */
        private String message;
        /** 会话唯一标识，首次请求时可为空，服务端将自动生成 */
        private String conversationId;
    }

}
