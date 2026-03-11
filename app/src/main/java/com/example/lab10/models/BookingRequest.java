package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body cho POST /api/booking
 * Server tự lấy userId từ JWT token — không cần gửi userId.
 */
public class BookingRequest {
    @SerializedName("showtimeDetailId")
    private Long showtimeDetailId;

    @SerializedName("seatIds")
    private List<Long> seatIds;

    public BookingRequest(Long showtimeDetailId, List<Long> seatIds) {
        this.showtimeDetailId = showtimeDetailId;
        this.seatIds = seatIds;
    }

    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public void setShowtimeDetailId(Long id) { this.showtimeDetailId = id; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
}
