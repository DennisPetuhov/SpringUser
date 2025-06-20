package com.example.user.controller.kafka

import com.example.user.service.KafkaProducerService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kafka")
class KafkaController(
    private val kafkaProducerService: KafkaProducerService
) {
    
    @PostMapping("/send")
    suspend fun sendMessage(@RequestParam message: String): ResponseEntity<String> {
        return try {
            kafkaProducerService.sendMessage(message)
            ResponseEntity.ok("Message '$message' sent successfully to topic")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to send message: ${e.message}")
        }
    }
    
    @PostMapping("/send-to-topic")
    suspend fun sendMessageToTopic(
        @RequestParam topic: String,
        @RequestParam message: String
    ): ResponseEntity<String> {
        return try {
            kafkaProducerService.sendMessageToTopic(topic, message)
            ResponseEntity.ok("Message '$message' sent successfully to topic '$topic'")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to send message: ${e.message}")
        }
    }
    
    @PostMapping("/send-with-key")
    suspend fun sendMessageWithKey(
        @RequestParam key: String,
        @RequestParam message: String
    ): ResponseEntity<String> {
        return try {
            kafkaProducerService.sendMessageWithKey(key, message)
            ResponseEntity.ok("Message '$message' with key '$key' sent successfully")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to send message: ${e.message}")
        }
    }
    
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("Kafka controller is running")
    }
}