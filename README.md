# WebSocket Chat Server (Java)
<img width="2410" height="1442" alt="image" src="https://github.com/user-attachments/assets/275b852b-8ae8-4389-a4e9-8eae6432e5c0" />

A multithreaded Java server that serves a browser UI and supports real-time chat over WebSocket.
Implemented using `ServerSocket`, custom HTTP request parsing, and manual WebSocket handshake/frame handling.

---

## Overview

This project includes:

- A thread-per-connection server using Java `ServerSocket`
- Manual HTTP request parsing (request line + headers)
- Static file serving (e.g., `resources/index.html`)
- WebSocket upgrade handshake (`Sec-WebSocket-Key` → `Sec-WebSocket-Accept`)
- Basic WebSocket frame processing (masked payload decoding)
- Room-based chat management with server-side broadcasting

---

## Features

### Multithreaded Server
- Accepts incoming connections and handles each client in a dedicated worker thread (`Runnable`)

### HTTP Handling
- Parses HTTP requests without external frameworks
- Serves static client files for the browser UI

### WebSocket Support
- Detects upgrade requests and completes the handshake using SHA-1 + Base64
- Reads and decodes WebSocket frames, including mask/unmask handling

### Room-based Messaging
- Supports joining/leaving named rooms
- Broadcasts messages to connected clients within the same room
- Maintains room state on the server side

---

## Project Structure

- `MyHttpServer.java` – Server entry point (listens and accepts connections)
- `MyRunnable.java` – Per-connection worker logic
- `HTTPRequest.java` – HTTP request parsing
- `HTTPResponse.java` – Static responses + WebSocket handshake/frame handling
- `Room.java` – Room state and broadcast management
- `resources/index.html` – Browser client UI

---

## Skills Demonstrated

- Concurrent programming (thread-per-connection model)
- Socket programming with `ServerSocket`
- Protocol-level implementation (HTTP parsing, WebSocket handshake)
- WebSocket frame decoding and masked payload handling
- Server-side state management (rooms and connected clients)
