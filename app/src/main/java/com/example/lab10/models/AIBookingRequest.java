package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body cho POST /api/booking/ai-booking
 * Tạo booking qua AI chat.
 */
public class AIBookingRequest {
    @SerializedName("showtimeDetailId")
    private Long showtimeDetailId;

    @SerializedName("seatIds")
    private List<Long> seatIds;

    @SerializedName("message")
    private String message;

    public AIBookingRequest(Long showtimeDetailId, List<Long> seatIds, String message) {
        this.showtimeDetailId = showtimeDetailId;
        this.seatIds = seatIds;
        this.message = message;
    }

    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public List<Long> getSeatIds() { return seatIds; }
    public String getMessage() { return message; }
}
