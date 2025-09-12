package com.teckit.festival.service;

import com.teckit.festival.dto.request.AiChatRequestDTO;
import com.teckit.festival.dto.response.AiChatResponseDTO;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final WebClient aiWebClient;

    public AiChatResponseDTO callAiChat(String question) {
        AiChatRequestDTO request = new AiChatRequestDTO(question);

        AiChatResponseDTO response = aiWebClient.post()
                .uri("/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiChatResponseDTO.class)
                .block();

        if (response == null) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_FAILED);
        }

        return response;
    }
}
