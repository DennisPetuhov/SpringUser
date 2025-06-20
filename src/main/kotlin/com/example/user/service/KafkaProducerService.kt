package com.example.user.service

import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    private val defaultTopic = "test_topic"
    
    suspend fun sendMessage(message: String): SendResult<String, String> {
        logger.info("Sending message to default topic: $message")
        return kafkaTemplate.send(defaultTopic, message).await()
    }
    
    suspend fun sendMessageToTopic(topic: String, message: String): SendResult<String, String> {
        logger.info("Sending message to topic '$topic': $message")
        return kafkaTemplate.send(topic, message).await()
    }
    
    suspend fun sendMessageWithKey(key: String, message: String): SendResult<String, String> {
        logger.info("Sending message with key '$key' to default topic: $message")
        return kafkaTemplate.send(defaultTopic, key, message).await()
    }
    
    suspend fun sendMessageWithKeyToTopic(
        topic: String, 
        key: String, 
        message: String
    ): SendResult<String, String> {
        logger.info("Sending message with key '$key' to topic '$topic': $message")
        return kafkaTemplate.send(topic, key, message).await()
    }
} 