package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeatResponse {

    @SerializedName("showtimeDetailId")
    private Long showtimeDetailId;

    @SerializedName("cinemaRoomName")
    private String cinemaRoomName;

    @SerializedName("seats")
    private List<Seat> seats;

    public List<Seat> getSeats() { return seats; }
    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public String getCinemaRoomName() { return cinemaRoomName; }
}