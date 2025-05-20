package com.example.user.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

@Component
class WebSocketHandler : TextWebSocketHandler() {
    
    private val logger = LoggerFactory.getLogger(WebSocketHandler::class.java)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // Store the session
        sessions[session.id] = session
        logger.info("WebSocket connection established. Session ID: {}", session.id)
        
        // Send a handshake confirmation message
        val handshakeMessage = TextMessage("CONNECTED:${session.id}")
        session.sendMessage(handshakeMessage)
    }
    
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        logger.info("Received message: {} from session: {}", payload, session.id)
        
        // Echo the message back to the sender as confirmation
        session.sendMessage(TextMessage("RECEIVED: $payload"))
        
        // Broadcast message to all connected clients except the sender
        broadcastMessage(session.id, payload)
    }
    
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        // Remove the session
        sessions.remove(session.id)
        logger.info("WebSocket connection closed. Session ID: {}, Status: {}", session.id, status)
    }
    
    /**
     * Broadcast a message to all connected sessions except the sender
     */
    fun broadcastMessage(senderSessionId: String, message: String) {
        sessions.forEach { (sessionId, session) ->
            if (sessionId != senderSessionId && session.isOpen) {
                try {
                    session.sendMessage(TextMessage(message))
                } catch (e: Exception) {
                    logger.error("Error sending message to session {}: {}", sessionId, e.message)
                }
            }
        }
    }
    
    /**
     * Send a message to a specific session
     */
    fun sendMessageToSession(sessionId: String, message: String): Boolean {
        val session = sessions[sessionId]
        return if (session != null && session.isOpen) {
            try {
                session.sendMessage(TextMessage(message))
                true
            } catch (e: Exception) {
                logger.error("Error sending message to session {}: {}", sessionId, e.message)
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Get all active session IDs
     */
    fun getActiveSessions(): List<String> {
        return sessions.keys().toList()
    }
} 