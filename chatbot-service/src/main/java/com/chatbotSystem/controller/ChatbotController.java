package com.chatbotSystem.controller;

import com.chatbotSystem.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<String> askChatbot(@RequestBody String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query cannot be empty.");
        }
        String response = chatbotService.getResponse(query);
        return ResponseEntity.ok(response);
    }
}
