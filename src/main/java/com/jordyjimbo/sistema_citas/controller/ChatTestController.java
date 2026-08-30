package com.jordyjimbo.sistema_citas.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatTestController {

    private final ChatModel chatModel;

    public ChatTestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/api/chat-test")
    public String probar(@RequestParam(defaultValue = "Hola, ¿quién eres?") String mensaje) {
        return chatModel.call(mensaje);
    }
}