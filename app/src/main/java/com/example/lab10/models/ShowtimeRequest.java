package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class ShowtimeRequest {

    @SerializedName("movieId")
    private Long movieId;

    @SerializedName("cinemaRoomId")
    private Long cinemaRoomId;

    @SerializedName("startTime")
    private String startTime; // ISO format: "2026-03-20T10:00:00"

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("basePrice")
    private Double basePrice;

    public ShowtimeRequest(Long movieId, Long cinemaRoomId,
                           String startTime, String endTime, Double basePrice) {
        this.movieId = movieId;
        this.cinemaRoomId = cinemaRoomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
    }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getCinemaRoomId() { return cinemaRoomId; }
    public void setCinemaRoomId(Long cinemaRoomId) { this.cinemaRoomId = cinemaRoomId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
}