package com.example.lab10.utils;

import com.example.lab10.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton giữ lịch sử chat xuyên suốt các Activity.
 * Chat history sẽ được giữ nguyên khi navigate giữa ChatActivity và SeatSelectionActivity.
 */
public class ChatHistoryManager {
    private static ChatHistoryManager instance;
    private final List<ChatMessage> messages = new ArrayList<>();

    private ChatHistoryManager() {}

    public static synchronized ChatHistoryManager getInstance() {
        if (instance == null) {
            instance = new ChatHistoryManager();
        }
        return instance;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public void removeLastIfLoading() {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            if (messages.get(lastIndex).isLoading()) {
                messages.remove(lastIndex);
            }
        }
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public void clear() {
        messages.clear();
    }
}
