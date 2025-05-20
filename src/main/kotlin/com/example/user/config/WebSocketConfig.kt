package com.example.user.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.beans.factory.annotation.Autowired
import com.example.user.handler.WebSocketHandler

@Configuration
@EnableWebSocket
class WebSocketConfig @Autowired constructor(
    private val webSocketHandler: WebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSocketHandler, "/ws")
            .setAllowedOrigins("*") // In production, specify exact origins
    }
} 