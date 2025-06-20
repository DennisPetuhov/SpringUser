package com.example.user.controller

import com.example.user.service.VideoControlService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller
class VideoControlController @Autowired constructor(
    private val messagingTemplate: SimpMessagingTemplate,
    private val videoControlService: VideoControlService
) {
    private val logger = LoggerFactory.getLogger(VideoControlController::class.java)
    
    @MessageMapping("/video/play")
    @SendTo("/topic/video/control")
    fun handlePlay(request: VideoControlRequest): VideoControlResponse {
        logger.info("Video play request: {}", request)
        val response = VideoControlResponse(
            action = "play",
            videoId = request.videoId,
            timestamp = request.timestamp,
            userId = request.userId,
            serverTime = LocalDateTime.now()
        )
        videoControlService.updateVideoState(request.videoId, "playing", request.timestamp)
        return response
    }
    
    @MessageMapping("/video/pause")
    @SendTo("/topic/video/control")
    fun handlePause(request: VideoControlRequest): VideoControlResponse {
        logger.info("Video pause request: {}", request)
        val response = VideoControlResponse(
            action = "pause",
            videoId = request.videoId,
            timestamp = request.timestamp,
            userId = request.userId,
            serverTime = LocalDateTime.now()
        )
        videoControlService.updateVideoState(request.videoId, "paused", request.timestamp)
        return response
    }
    
    @MessageMapping("/video/seek")
    @SendTo("/topic/video/control")
    fun handleSeek(request: VideoControlRequest): VideoControlResponse {
        logger.info("Video seek request: {}", request)
        val response = VideoControlResponse(
            action = "seek",
            videoId = request.videoId,
            timestamp = request.timestamp,
            userId = request.userId,
            serverTime = LocalDateTime.now()
        )
        videoControlService.updateVideoState(request.videoId, "seeking", request.timestamp)
        return response
    }
    
    @MessageMapping("/video/join")
    fun handleJoin(request: VideoJoinRequest) {
        logger.info("User joining video: {}", request)
        videoControlService.addViewer(request.videoId, request.userId)
        
        // Send current state to the joining user
        val currentState = videoControlService.getCurrentState(request.videoId)
        if (currentState != null) {
            messagingTemplate.convertAndSendToUser(
                request.userId,
                "/queue/video/state",
                currentState
            )
        }
        
        // Broadcast updated viewer count
        val viewerCount = videoControlService.getViewerCount(request.videoId)
        messagingTemplate.convertAndSend(
            "/topic/video/viewers",
            ViewerCountResponse(request.videoId, viewerCount)
        )
    }
    
    @MessageMapping("/video/leave")
    fun handleLeave(request: VideoLeaveRequest) {
        logger.info("User leaving video: {}", request)
        videoControlService.removeViewer(request.videoId, request.userId)
        
        // Broadcast updated viewer count
        val viewerCount = videoControlService.getViewerCount(request.videoId)
        messagingTemplate.convertAndSend(
            "/topic/video/viewers",
            ViewerCountResponse(request.videoId, viewerCount)
        )
    }
    
    @MessageMapping("/video/chat")
    @SendTo("/topic/video/chat")
    fun handleChat(request: VideoChatRequest): VideoChatResponse {
        logger.info("Video chat message: {}", request)
        return VideoChatResponse(
            videoId = request.videoId,
            userId = request.userId,
            username = request.username,
            message = request.message,
            timestamp = LocalDateTime.now()
        )
    }
}

data class VideoControlRequest(
    val videoId: String,
    val userId: String,
    val timestamp: Double
)

data class VideoControlResponse(
    val action: String,
    val videoId: String,
    val timestamp: Double,
    val userId: String,
    val serverTime: LocalDateTime
)

data class VideoJoinRequest(
    val videoId: String,
    val userId: String
)

data class VideoLeaveRequest(
    val videoId: String,
    val userId: String
)

data class ViewerCountResponse(
    val videoId: String,
    val count: Int
)

data class VideoChatRequest(
    val videoId: String,
    val userId: String,
    val username: String,
    val message: String
)

data class VideoChatResponse(
    val videoId: String,
    val userId: String,
    val username: String,
    val message: String,
    val timestamp: LocalDateTime
)

data class VideoState(
    val videoId: String,
    val state: String, // playing, paused, seeking
    val timestamp: Double,
    val lastUpdated: LocalDateTime
) 