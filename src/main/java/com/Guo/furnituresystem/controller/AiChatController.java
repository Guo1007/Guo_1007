package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.AiChatDTO;
import com.Guo.furnituresystem.service.IAiChatService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Resource
    private IAiChatService aiChatService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody AiChatDTO dto) {
        return aiChatService.chatStream(dto);
    }
}