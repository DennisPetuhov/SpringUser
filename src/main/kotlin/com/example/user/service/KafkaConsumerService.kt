package com.example.user.service

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService {
    
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    
    @KafkaListener(topics = ["test_topic"], groupId = "test_id")
    fun consumeMessage(@Payload message: String) {
        logger.info("Message received from topic 'test_topic': $message")
        processMessage(message)
    }
    
    @KafkaListener(topics = ["test_topic"], groupId = "test_id_with_details")
    fun consumeMessageWithDetails(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        logger.info("Detailed message received - Topic: ${consumerRecord.topic()}, " +
                   "Partition: ${consumerRecord.partition()}, " +
                   "Offset: ${consumerRecord.offset()}, " +
                   "Key: ${consumerRecord.key()}, " +
                   "Value: ${consumerRecord.value()}")
        
        try {
            processMessage(consumerRecord.value())
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing message: ${e.message}", e)
            // Don't acknowledge on error - message will be retried
        }
    }
    
    @KafkaListener(topics = ["#{'\${kafka.topics.dynamic:dynamic_topic}'}"], groupId = "dynamic_group")
    fun consumeDynamicTopic(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        logger.info("Message from dynamic topic '$topic' [partition: $partition, offset: $offset]: $message")
        processMessage(message)
    }
    
    private fun processMessage(message: String) {
        // Add your business logic here
        logger.info("Processing message: $message")
        
        // Simulate some processing time
        Thread.sleep(100)
        
        logger.info("Message processed successfully: $message")
    }
} 