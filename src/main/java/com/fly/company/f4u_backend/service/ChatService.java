package com.fly.company.f4u_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fly.company.f4u_backend.model.ChatMessage;
import com.fly.company.f4u_backend.repository.ChatMessageRepository;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Guardar un nuevo mensaje en la base de datos
     */
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    /**
     * Obtener todos los mensajes entre dos usuarios (enviados y recibidos)
     */
    public List<ChatMessage> getMessages(String userId, String otherUserId) {
        return chatMessageRepository.findMessagesBetweenUsers(userId, otherUserId);
    }

    /**
     * Obtener la lista de contactos del usuario (usuarios con los que ha chateado)
     */
    public List<String> getChatContacts(String userId) {
        return chatMessageRepository.findChatContactsByUserId(userId);
    }

    /**
     * Obtener todos los mensajes enviados por un usuario
     */
    public List<ChatMessage> getSentMessages(String userId) {
        return chatMessageRepository.findMessagesBySenderId(userId);
    }

    /**
     * Obtener todos los mensajes recibidos por un usuario
     */
    public List<ChatMessage> getReceivedMessages(String userId) {
        return chatMessageRepository.findMessagesByReceiverId(userId);
    }

    /**
     * Marcar mensaje como leído
     */
    public ChatMessage markAsRead(String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus("READ");
        return chatMessageRepository.save(message);
    }
}
