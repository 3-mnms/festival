package com.teckit.festival.controller;

import com.teckit.festival.dto.request.AiChatRequestDTO;
import com.teckit.festival.dto.response.AiChatResponseDTO;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.AiChatService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@RestController
@RequestMapping("/api/festival")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "챗봇 질의", description = "사용자의 질문을 받아 AI 챗봇 응답을 반환합니다.")
    @PostMapping("/chat")
    public ResponseEntity<SuccessResponse<AiChatResponseDTO>> chat(@RequestBody AiChatRequestDTO request) {
        AiChatResponseDTO response = aiChatService.callAiChat(request.getQuestion());
        return ApiResponseUtil.success(response, "AI 챗봇 응답 성공");
    }
}