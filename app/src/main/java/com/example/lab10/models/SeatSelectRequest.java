package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeatSelectRequest {
    @SerializedName("seatIds")
    private List<Long> seatIds;

    @SerializedName("showtimeId")
    private Long showtimeId;

    public SeatSelectRequest(List<Long> seatIds, Long showtimeId) {
        this.seatIds = seatIds;
        this.showtimeId = showtimeId;
    }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
}
