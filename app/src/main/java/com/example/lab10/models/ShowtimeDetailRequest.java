package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class ShowtimeDetailRequest {
    @SerializedName("basePrice") private Double basePrice;
    @SerializedName("startTime") private String startTime;

    public ShowtimeDetailRequest(Double basePrice, String startTime) {
        this.basePrice = basePrice;
        this.startTime = startTime;
    }
}