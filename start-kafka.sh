#!/bin/bash

echo "🚀 Starting Kafka infrastructure..."

# Start Kafka and Zookeeper
docker-compose up -d zookeeper kafka kafka-ui

echo "⏳ Waiting for Kafka to be ready..."
sleep 15

# Check if Kafka is running
if docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo "✅ Kafka is ready!"
    
    # Create test topic if it doesn't exist
    echo "📝 Creating test topics..."
    docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic test_topic --partitions 3 --replication-factor 1
    docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dynamic_topic --partitions 3 --replication-factor 1
    
    echo "📋 Available topics:"
    docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
    
    echo ""
    echo "🎉 Kafka setup complete!"
    echo "📊 Kafka UI available at: http://localhost:8090"
    echo "🔗 Kafka broker at: localhost:9092"
    echo ""
    echo "To start your Spring Boot app:"
    echo "  ./gradlew bootRun"
    echo ""
    echo "To test Kafka:"
    echo "  curl -X POST 'http://localhost:8080/api/kafka/send?message=Hello World'"
else
    echo "❌ Kafka failed to start properly"
    exit 1
fi 