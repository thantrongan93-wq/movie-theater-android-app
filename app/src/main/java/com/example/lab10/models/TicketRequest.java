package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketRequest {
    @SerializedName("showtimeId")
    private Long showtimeId;

    @SerializedName("seatIds")
    private List<Long> seatIds;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("totalPrice")
    private Double totalPrice;

    public TicketRequest(Long showtimeId, List<Long> seatIds, Long userId, Double totalPrice) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.userId = userId;
        this.totalPrice = totalPrice;
    }

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}
