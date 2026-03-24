package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /api/chat-with-User
 * Gửi tin nhắn tới AI chatbot backend.
 */
public class ChatRequest {
    @SerializedName("message")
    private String message;

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
