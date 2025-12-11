package com.fly.company.f4u_backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fly.company.f4u_backend.repository.ChatMessageRepository;

@RestController
public class TestController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/api/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    // Endpoint para borrar todos los chats (SOLO PARA DESARROLLO)
    @DeleteMapping("/api/test/chats")
    public Map<String, Object> deleteAllChats() {
        long count = chatMessageRepository.count();
        chatMessageRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Se eliminaron " + count + " mensajes de chat.");
        return response;
    }
}
