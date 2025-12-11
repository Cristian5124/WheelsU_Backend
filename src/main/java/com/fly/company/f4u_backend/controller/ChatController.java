package com.fly.company.f4u_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fly.company.f4u_backend.model.ChatMessage;
import com.fly.company.f4u_backend.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar mensaje vía REST (fallback)
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody ChatMessage message) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Guardar mensaje (sin validación JWT ya que la seguridad está desactivada)
            ChatMessage savedMessage = chatService.saveMessage(message);
            
            // Enviar mensaje al receptor vía WebSocket
            messagingTemplate.convertAndSendToUser(
                message.getReceiverId(), 
                "/queue/messages", 
                savedMessage
            );
            
            response.put("success", true);
            response.put("message", savedMessage);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending message: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtener mensajes entre dos usuarios (enviados y recibidos)
     */
    @GetMapping("/messages/{userId}/{otherUserId}")
    public ResponseEntity<Map<String, Object>> getMessages(
            @PathVariable String userId,
            @PathVariable String otherUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ChatMessage> messages = chatService.getMessages(userId, otherUserId);
            
            response.put("success", true);
            response.put("messages", messages);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching messages: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public ChatController() {
        System.out.println("🚀 ChatController initialized!");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Obtener lista de contactos (usuarios con los que ha chateado)
     */
    @GetMapping("/contacts")
    public ResponseEntity<Map<String, Object>> getContacts(@org.springframework.web.bind.annotation.RequestParam(required = false) String userId) {
        
        System.out.println("🔍 DEBUG: getContacts called. UserId: " + userId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId == null) {
                userId = "default-user";
                System.out.println("⚠️ No userId provided, using default: " + userId);
            }

            System.out.println("🔄 Calling chatService.getChatContacts(" + userId + ")...");
            List<String> contacts = chatService.getChatContacts(userId);
            System.out.println("✅ Service returned " + contacts.size() + " contacts");
            
            response.put("success", true);
            response.put("contacts", contacts);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in getContacts:");
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching contacts: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Enviar mensaje vía WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessageWebSocket(@Payload ChatMessage message) {
        ChatMessage savedMessage = chatService.saveMessage(message);
        
        // Enviar al receptor específico
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId(),
            "/queue/messages",
            savedMessage
        );
        
        return savedMessage;
    }

    /**
     * Usuario se conecta al chat
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message) {
        message.setStatus("JOINED");
        return message;
    }

    /**
     * Marcar mensaje como leído
     */
    @PostMapping("/read/{messageId}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable String messageId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ChatMessage message = chatService.markAsRead(messageId);
            
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking message as read: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
