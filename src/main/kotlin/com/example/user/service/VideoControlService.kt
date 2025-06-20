package com.example.user.service

import com.example.user.controller.VideoState
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class VideoControlService {
    private val logger = LoggerFactory.getLogger(VideoControlService::class.java)
    
    // Store video states
    private val videoStates = ConcurrentHashMap<String, VideoState>()
    
    // Store viewers for each video
    private val videoViewers = ConcurrentHashMap<String, MutableSet<String>>()
    
    fun updateVideoState(videoId: String, state: String, timestamp: Double) {
        val videoState = VideoState(
            videoId = videoId,
            state = state,
            timestamp = timestamp,
            lastUpdated = LocalDateTime.now()
        )
        videoStates[videoId] = videoState
        logger.debug("Updated video state for {}: {} at {}", videoId, state, timestamp)
    }
    
    fun getCurrentState(videoId: String): VideoState? {
        return videoStates[videoId]
    }
    
    fun addViewer(videoId: String, userId: String) {
        videoViewers.computeIfAbsent(videoId) { mutableSetOf() }.add(userId)
        logger.debug("Added viewer {} to video {}", userId, videoId)
    }
    
    fun removeViewer(videoId: String, userId: String) {
        videoViewers[videoId]?.remove(userId)
        // Clean up empty sets
        if (videoViewers[videoId]?.isEmpty() == true) {
            videoViewers.remove(videoId)
            // Optionally clean up video state when no viewers
            videoStates.remove(videoId)
            logger.debug("Cleaned up video {} - no more viewers", videoId)
        }
        logger.debug("Removed viewer {} from video {}", userId, videoId)
    }
    
    fun getViewerCount(videoId: String): Int {
        return videoViewers[videoId]?.size ?: 0
    }
    
    fun getViewers(videoId: String): Set<String> {
        return videoViewers[videoId]?.toSet() ?: emptySet()
    }
    
    fun getAllActiveVideos(): Map<String, VideoInfo> {
        return videoStates.mapValues { (videoId, state) ->
            VideoInfo(
                videoId = videoId,
                state = state.state,
                timestamp = state.timestamp,
                viewerCount = getViewerCount(videoId),
                lastUpdated = state.lastUpdated
            )
        }
    }
    
    fun isUserWatchingVideo(videoId: String, userId: String): Boolean {
        return videoViewers[videoId]?.contains(userId) ?: false
    }
}

data class VideoInfo(
    val videoId: String,
    val state: String,
    val timestamp: Double,
    val viewerCount: Int,
    val lastUpdated: LocalDateTime
) 