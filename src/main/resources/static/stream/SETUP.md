# Video Streaming Setup

## Overview
This application provides HTTP video streaming with byte-range support and WebSocket-based synchronization controls.

## Features
- **HTTP Byte-Range Streaming**: Proper video seeking and buffering support
- **Synchronized Playback**: Real-time play/pause/seek synchronization across clients
- **Live Chat**: Real-time chat during video playback
- **Viewer Count**: Live viewer counter
- **Multiple Video Support**: Support for various video formats (MP4, WebM, OGG, AVI, MOV)

## Setup Instructions

### 1. Add Video Files
Place your video files in this directory (`src/main/resources/static/stream/`):
- Supported formats: `.mp4`, `.webm`, `.ogg`, `.avi`, `.mov`
- Example: `hello.mp4`

### 2. Access the Application
- Video Player: `http://localhost:8080/video-test.html`
- Available Videos API: `http://localhost:8080/api/video-management/available`
- Active Sessions API: `http://localhost:8080/api/video-management/sessions`

### 3. API Endpoints

#### Video Streaming
- `GET /api/video/stream/{filename}` - Stream video with byte-range support
- `GET /api/video/info/{filename}` - Get video information

#### Session Management
- `GET /api/video-management/sessions` - List all active video sessions
- `GET /api/video-management/sessions/{videoId}` - Get specific session info
- `POST /api/video-management/sessions/{videoId}/reset` - Reset video session

#### WebSocket Endpoints (STOMP)
- `/app/video/join` - Join a video session
- `/app/video/leave` - Leave a video session
- `/app/video/play` - Sync play command
- `/app/video/pause` - Sync pause command
- `/app/video/seek` - Sync seek command
- `/app/video/chat` - Send chat message

#### WebSocket Subscriptions
- `/topic/video/control` - Video control events
- `/topic/video/viewers` - Viewer count updates
- `/topic/video/chat` - Chat messages
- `/user/queue/video/state` - Personal video state updates

## Usage Example

1. Start the Spring Boot application
2. Place a video file (e.g., `hello.mp4`) in this directory
3. Open `http://localhost:8080/video-test.html` in multiple browser tabs
4. Enter different User IDs and join the same video session
5. Use sync controls to synchronize playback across all clients
6. Use chat to communicate during playback

## Technical Details

### HTTP Byte-Range Support
The video streaming controller supports HTTP Range requests, enabling:
- Proper video seeking
- Efficient buffering
- Partial content delivery
- Better user experience for large video files

### WebSocket Synchronization
Real-time synchronization is achieved through STOMP over WebSocket:
- Server maintains video state (playing/paused/seeking)
- All clients receive sync commands
- Automatic state synchronization for new joiners
- Real-time viewer count updates

### Video State Management
- In-memory storage of video states and viewer lists
- Automatic cleanup when no viewers remain
- Thread-safe concurrent access using ConcurrentHashMap 