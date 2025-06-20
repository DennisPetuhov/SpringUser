package com.example.user.controller

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRange
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/api/video")
class VideoStreamController(
    private val resourceLoader: ResourceLoader
) {
    private val logger = LoggerFactory.getLogger(VideoStreamController::class.java)

    @GetMapping("/stream/{filename}")
    fun streamVideo(
        @PathVariable filename: String, request: HttpServletRequest
    ): ResponseEntity<ByteArray> {
        try {
            val resource = resourceLoader.getResource("classpath:static/stream/$filename")

            if (!resource.exists()) {
                logger.warn("Video file not found: $filename")
                return ResponseEntity.notFound().build()
            }

            val contentLength = resource.contentLength()
            val rangeHeader = request.getHeader(HttpHeaders.RANGE)

            return if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                handleRangeRequest(resource, rangeHeader, contentLength, filename)
            } else {
                handleFullRequest(resource, contentLength, filename)
            }

        } catch (e: IOException) {
            logger.error("Error streaming video: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun handleRangeRequest(
        resource: Resource, rangeHeader: String, contentLength: Long, filename: String
    ): ResponseEntity<ByteArray> {
        val ranges = HttpRange.parseRanges(rangeHeader)

        if (ranges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */$contentLength").build()
        }

        val range = ranges[0]
        val start = range.getRangeStart(contentLength)
        val end = range.getRangeEnd(contentLength)
        val rangeLength = end - start + 1

        logger.debug("Range request for $filename: bytes=$start-$end/$contentLength")

        return try {
            resource.inputStream.use { inputStream ->
                inputStream.skip(start)
                val buffer = ByteArray(rangeLength.toInt())
                val bytesRead = inputStream.read(buffer)

                if (bytesRead == -1) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build()
                }

                val actualBuffer = if (bytesRead < buffer.size) {
                    buffer.copyOf(bytesRead)
                } else {
                    buffer
                }

                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(filename))
                    .header(HttpHeaders.CONTENT_LENGTH, actualBuffer.size.toString())
                    .header(HttpHeaders.CONTENT_RANGE, "bytes $start-${start + actualBuffer.size - 1}/$contentLength")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes").body(actualBuffer)
            }
        } catch (e: IOException) {
            logger.error("Error reading range from video file: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun handleFullRequest(
        resource: Resource, contentLength: Long, filename: String
    ): ResponseEntity<ByteArray> {
        return try {
            val bytes = resource.inputStream.readAllBytes()

            ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, getContentType(filename))
                .header(HttpHeaders.CONTENT_LENGTH, contentLength.toString()).header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(bytes)
        } catch (e: IOException) {
            logger.error("Error reading full video file: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun getContentType(filename: String): String {
        return when (filename.substringAfterLast('.').lowercase()) {
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "ogg" -> "video/ogg"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }

    @GetMapping("/info/{filename}")
    fun getVideoInfo(@PathVariable filename: String): ResponseEntity<VideoInfo> {
        try {
            val resource = resourceLoader.getResource("classpath:static/stream/$filename")

            if (!resource.exists()) {
                return ResponseEntity.notFound().build()
            }

            val videoInfo = VideoInfo(
                filename = filename,
                size = resource.contentLength(),
                contentType = getContentType(filename),
                supportsRangeRequests = true
            )

            return ResponseEntity.ok(videoInfo)
        } catch (e: IOException) {
            logger.error("Error getting video info: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}

data class VideoInfo(
    val filename: String, val size: Long, val contentType: String, val supportsRangeRequests: Boolean
) 