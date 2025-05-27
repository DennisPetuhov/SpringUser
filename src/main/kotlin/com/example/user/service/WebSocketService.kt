package com.example.user.service

import com.example.user.handler.WebSocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@Service
class WebSocketService @Autowired constructor(
    private val webSocketHandler: WebSocketHandler
) {
    private val logger = LoggerFactory.getLogger(WebSocketService::class.java)
    private val objectMapper = ObjectMapper().registerKotlinModule()
    
    /**
     * Send a structured message to a specific client
     */
    fun sendMessage(sessionId: String, type: String, payload: Any): Boolean {
        try {
            val message = WebSocketMessage(type, payload)
            val messageJson = objectMapper.writeValueAsString(message)
            return webSocketHandler.sendMessageToSession(sessionId, messageJson)
        } catch (e: Exception) {
            logger.error("Error serializing message: {}", e.message)
            return false
        }
    }
    
    /**
     * Broadcast a structured message to all clients
     */
    fun broadcastMessage(type: String, payload: Any) {
        try {
            val message = WebSocketMessage(type, payload)
            val messageJson = objectMapper.writeValueAsString(message)
            webSocketHandler.broadcastMessage("SERVER", messageJson)
        } catch (e: Exception) {
            logger.error("Error serializing broadcast message: {}", e.message)
        }
    }
    
    /**
     * Parse an incoming WebSocket message
     */
    fun parseMessage(message: String): WebSocketMessage<*>? {
        return try {
            objectMapper.readValue(message, WebSocketMessage::class.java)
        } catch (e: Exception) {
            logger.error("Error parsing message: {}", e.message)
            null
        }
    }

    fun getActiveSessions(): List<String> {
        return webSocketHandler.getActiveSessions()
    }
}

data class WebSocketMessage<T>(
    val type: String,
    val payload: T
) 