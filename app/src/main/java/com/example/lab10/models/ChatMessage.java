package com.example.lab10.models;

import java.util.List;

/**
 * Model cho mỗi tin nhắn trong UI chatbox.
 * Hỗ trợ: tin nhắn text, action xem ghế, QR payment, loading indicator.
 */
public class ChatMessage {
    private String message;
    private boolean isUser;
    private long timestamp;
    private boolean isLoading;

    // Parsed action data từ AI response
    private Long movieId;
    private Long showtimeDetailId;

    // Booking action data (khi AI detect intent đặt vé)
    private List<String> seatNames;
    private String bookingId;

    // QR payment display
    private String qrImageUrl;
    private String bankName;
    private String bankAccount;
    private String paymentAmount;
    private String paymentContent;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
    }

    /** Constructor cho loading indicator */
    public static ChatMessage createLoading() {
        ChatMessage msg = new ChatMessage("", false);
        msg.isLoading = true;
        return msg;
    }

    /** Factory cho QR payment message */
    public static ChatMessage createQrMessage(String qrImageUrl, String bankName,
                                               String bankAccount, String amount,
                                               String content, String bookingId) {
        ChatMessage msg = new ChatMessage("", false);
        msg.qrImageUrl = qrImageUrl;
        msg.bankName = bankName;
        msg.bankAccount = bankAccount;
        msg.paymentAmount = amount;
        msg.paymentContent = content;
        msg.bookingId = bookingId;
        return msg;
    }

    // ======= Getters and Setters =======
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }

    public long getTimestamp() { return timestamp; }

    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { isLoading = loading; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public void setShowtimeDetailId(Long showtimeDetailId) { this.showtimeDetailId = showtimeDetailId; }

    public boolean hasAction() { return movieId != null && showtimeDetailId != null; }

    public List<String> getSeatNames() { return seatNames; }
    public void setSeatNames(List<String> seatNames) { this.seatNames = seatNames; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getQrImageUrl() { return qrImageUrl; }
    public boolean isQrMessage() { return qrImageUrl != null && !qrImageUrl.isEmpty(); }

    public String getBankName() { return bankName; }
    public String getBankAccount() { return bankAccount; }
    public String getPaymentAmount() { return paymentAmount; }
    public String getPaymentContent() { return paymentContent; }
}
