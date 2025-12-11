package com.fly.company.f4u_backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fly.company.f4u_backend.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Obtener todos los mensajes entre dos usuarios (enviados y recibidos)
     * Ordenados por timestamp ascendente (más antiguos primero)
     */
    @Query(value = "{ '$or': [ { 'sender_id': ?0, 'receiver_id': ?1 }, { 'sender_id': ?1, 'receiver_id': ?0 } ] }", sort = "{ 'timestamp' : 1 }")
    List<ChatMessage> findMessagesBetweenUsers(String userId, String otherUserId);

    /**
     * Obtener la lista de usuarios con los que el usuario ha chateado
     * (usuarios únicos que son sender o receiver)
     */
    @Aggregation(pipeline = {
        "{ '$match': { '$or': [ { 'sender_id': ?0 }, { 'receiver_id': ?0 } ] } }",
        "{ '$project': { 'contact': { '$cond': { 'if': { '$eq': [ '$sender_id', ?0 ] }, 'then': '$receiver_id', 'else': '$sender_id' } } } }",
        "{ '$group': { '_id': '$contact' } }"
    })
    List<String> findChatContactsByUserId(String userId);

    /**
     * Obtener todos los mensajes enviados por un usuario
     */
    @Query(value = "{ 'sender_id': ?0 }", sort = "{ 'timestamp' : -1 }")
    List<ChatMessage> findMessagesBySenderId(String userId);

    /**
     * Obtener todos los mensajes recibidos por un usuario
     */
    @Query(value = "{ 'receiver_id': ?0 }", sort = "{ 'timestamp' : -1 }")
    List<ChatMessage> findMessagesByReceiverId(String userId);
}
