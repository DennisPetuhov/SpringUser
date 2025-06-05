package com.example.user.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory

@Controller
class StompController @Autowired constructor(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(StompController::class.java)
    
    @MessageMapping("/send")
    @SendTo("/topic/messages")
    fun handleMessage(message: MessageRequest): MessageResponse {
        logger.info("Received message: {}", message)
        val response = MessageResponse(true, "Received: ${message.content}")
        logger.info("Sending response: {}", response)
        return response
    }
    
    @MessageMapping("/private")
    fun handlePrivateMessage(message: PrivateMessageRequest) {
        logger.info("Received private message: {}", message)
        messagingTemplate.convertAndSendToUser(
            message.recipientId,
            "/queue/private",
            MessageResponse(true, "Private message: ${message.content}")
        )
    }
}

data class PrivateMessageRequest(
    val recipientId: String,
    val content: String
) 