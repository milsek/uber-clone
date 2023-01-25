package com.example.springbackend.websocket;

public enum MessageType {
    CHAT,
    CONNECT,
    DISCONNECT,
    NOTIFICATION,
    RIDE_UPDATE,
    RIDE_ERROR,
    RIDE_COMPLETE,
    DISAPPEARING
}
