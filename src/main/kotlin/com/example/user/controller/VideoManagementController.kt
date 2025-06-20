package com.example.user.controller

import com.example.user.service.VideoControlService
import org.springframework.core.io.ResourceLoader
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.io.File

@RestController
@RequestMapping("/api/video-management")
class VideoManagementController(
    private val videoControlService: VideoControlService,
    private val resourceLoader: ResourceLoader
) {
    private val logger = LoggerFactory.getLogger(VideoManagementController::class.java)
    
    @GetMapping("/sessions")
    fun getActiveSessions(): ResponseEntity<Map<String, Any>> {
        val activeVideos = videoControlService.getAllActiveVideos()
        return ResponseEntity.ok(mapOf(
            "activeVideos" to activeVideos,
            "totalSessions" to activeVideos.size
        ))
    }
    
    @GetMapping("/sessions/{videoId}")
    fun getVideoSession(@PathVariable videoId: String): ResponseEntity<VideoSessionInfo> {
        val state = videoControlService.getCurrentState(videoId)
        val viewerCount = videoControlService.getViewerCount(videoId)
        val viewers = videoControlService.getViewers(videoId)
        
        val sessionInfo = VideoSessionInfo(
            videoId = videoId,
            state = state?.state ?: "unknown",
            timestamp = state?.timestamp ?: 0.0,
            viewerCount = viewerCount,
            viewers = viewers.toList(),
            lastUpdated = state?.lastUpdated
        )
        
        return ResponseEntity.ok(sessionInfo)
    }
    
    @GetMapping("/available")
    fun getAvailableVideos(): ResponseEntity<List<AvailableVideo>> {
        return try {
            val streamResource = resourceLoader.getResource("classpath:static/stream/")
            val streamDir = streamResource.file
            
            if (!streamDir.exists() || !streamDir.isDirectory) {
                return ResponseEntity.ok(emptyList())
            }
            
            val videoFiles = streamDir.listFiles { file ->
                file.isFile && file.name.matches(Regex(".*\\.(mp4|webm|ogg|avi|mov)$", RegexOption.IGNORE_CASE))
            }?.map { file ->
                AvailableVideo(
                    filename = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    streamUrl = "/api/video/stream/${file.name}",
                    infoUrl = "/api/video/info/${file.name}"
                )
            } ?: emptyList()
            
            ResponseEntity.ok(videoFiles)
        } catch (e: Exception) {
            logger.error("Error listing available videos: ${e.message}")
            ResponseEntity.ok(emptyList())
        }
    }
    
    @PostMapping("/sessions/{videoId}/reset")
    fun resetVideoSession(@PathVariable videoId: String): ResponseEntity<Map<String, String>> {
        // Remove all viewers and reset state
        val viewers = videoControlService.getViewers(videoId)
        viewers.forEach { userId ->
            videoControlService.removeViewer(videoId, userId)
        }
        
        return ResponseEntity.ok(mapOf(
            "message" to "Video session reset successfully",
            "videoId" to videoId
        ))
    }
}

data class VideoSessionInfo(
    val videoId: String,
    val state: String,
    val timestamp: Double,
    val viewerCount: Int,
    val viewers: List<String>,
    val lastUpdated: java.time.LocalDateTime?
)

data class AvailableVideo(
    val filename: String,
    val size: Long,
    val lastModified: Long,
    val streamUrl: String,
    val infoUrl: String
) 