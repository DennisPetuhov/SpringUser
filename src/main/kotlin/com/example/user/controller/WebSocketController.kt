package com.example.user.controller

import com.example.user.handler.WebSocketHandler
import com.example.user.service.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ws")
class WebSocketController @Autowired constructor(
    private val webSocketService: WebSocketService
) {

    @PostMapping("/broadcast")
    fun broadcast(@RequestBody message: MessageRequest): ResponseEntity<MessageResponse> {
        webSocketService.broadcastMessage("BROADCAST", message.content)
        return ResponseEntity.ok(MessageResponse(true, "Message broadcast to all clients"))
    }
    
    @PostMapping("/broadcast/structured")
    fun broadcastStructured(@RequestBody message: StructuredMessageRequest): ResponseEntity<MessageResponse> {
        webSocketService.broadcastMessage(message.type, message.payload)
        return ResponseEntity.ok(MessageResponse(true, "Structured message broadcast to all clients"))
    }
    
    @PostMapping("/send/{sessionId}")
    fun sendToSession(
        @PathVariable sessionId: String,
        @RequestBody message: MessageRequest
    ): ResponseEntity<MessageResponse> {
        val sent = webSocketService.sendMessage(sessionId, "DIRECT", message.content)
        return if (sent) {
            ResponseEntity.ok(MessageResponse(true, "Message sent to session $sessionId"))
        } else {
            ResponseEntity.ok(MessageResponse(false, "Failed to send message to session $sessionId"))
        }
    }
    
    @PostMapping("/send/{sessionId}/structured")
    fun sendStructuredToSession(
        @PathVariable sessionId: String,
        @RequestBody message: StructuredMessageRequest
    ): ResponseEntity<MessageResponse> {
        val sent = webSocketService.sendMessage(sessionId, message.type, message.payload)
        return if (sent) {
            ResponseEntity.ok(MessageResponse(true, "Structured message sent to session $sessionId"))
        } else {
            ResponseEntity.ok(MessageResponse(false, "Failed to send structured message to session $sessionId"))
        }
    }
    
    @GetMapping("/sessions")
    fun getActiveSessions(): ResponseEntity<SessionsResponse> {
        val sessions = webSocketService.getActiveSessions()
        return ResponseEntity.ok(SessionsResponse(sessions, sessions.size))
    }
}

data class MessageRequest(val content: String)
data class StructuredMessageRequest(val type: String, val payload: Any)
data class MessageResponse(val success: Boolean, val message: String)
data class SessionsResponse(val sessions: List<String>, val count: Int) 