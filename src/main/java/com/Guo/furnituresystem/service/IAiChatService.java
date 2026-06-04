package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.AiChatDTO;
import reactor.core.publisher.Flux;

public interface IAiChatService {

    Flux<String> chatStream(AiChatDTO dto);
}