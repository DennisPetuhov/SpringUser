package com.example.user.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory

@Controller
class DebugController {
    private val logger = LoggerFactory.getLogger(DebugController::class.java)
    
    @MessageMapping("/test")
    @SendTo("/topic/test")
    fun handleTest(message: Map<String, Any>): Map<String, Any> {
        logger.info("Debug test message received: {}", message)
        return mapOf(
            "response" to "Test successful",
            "received" to message,
            "timestamp" to System.currentTimeMillis()
        )
    }
} 