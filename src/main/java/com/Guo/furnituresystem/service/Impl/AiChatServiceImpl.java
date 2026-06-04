package com.Guo.furnituresystem.service.Impl;

import com.Guo.furnituresystem.entity.dto.AiChatDTO;
import com.Guo.furnituresystem.service.IAiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class AiChatServiceImpl implements IAiChatService {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-pro}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是家具商城的智能客服助手，名叫"小智"。你的职责是：
            1. 热情友好地回答用户关于家具产品的问题
            2. 帮助用户了解家具材质、尺寸、风格等信息
            3. 提供家具搭配建议和选购指南
            4. 解答订单、配送、售后等相关问题
            5. 如果用户询问非家具相关问题，礼貌地引导回家具话题
            
            请用简洁、友好的语言回答，必要时使用emoji表情让对话更亲切。
            """;

    @Override
    public Flux<String> chatStream(AiChatDTO dto) {
        if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
            return Flux.just("data: {\"error\": \"消息内容不能为空\"}\n\n");
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl + "/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ObjectNode requestBody = buildRequestBody(dto);

        return client.post()
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.equals("[DONE]"))
                .map(data -> {
                    try {
                        if (data.trim().isEmpty()) return "";
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode delta = node.path("choices").path(0).path("delta");
                        String content = delta.path("content").asText("");
                        if (!content.isEmpty()) {
                            return objectMapper.writeValueAsString(
                                    objectMapper.createObjectNode().put("content", content)
                            );
                        }
                    } catch (Exception e) {
                        // 忽略解析异常
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .concatWith(Flux.just("[DONE]"))
                .onErrorResume(e -> {
                    log.error("AI客服流式调用失败: {}", e.getMessage(), e);
                    return Flux.just("{\"error\": \"AI客服暂时无法响应\"}");
                });
    }

    private ObjectNode buildRequestBody(AiChatDTO dto) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("stream", true);
        requestBody.put("max_tokens", 2048);
        requestBody.put("temperature", 0.7);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        if (dto.getHistory() != null && !dto.getHistory().isEmpty()) {
            for (AiChatDTO.ChatMessage historyMsg : dto.getHistory()) {
                ObjectNode msg = objectMapper.createObjectNode();
                msg.put("role", historyMsg.getRole());
                msg.put("content", historyMsg.getContent());
                messages.add(msg);
            }
        }

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", dto.getMessage());
        messages.add(userMessage);

        requestBody.set("messages", messages);
        return requestBody;
    }
}