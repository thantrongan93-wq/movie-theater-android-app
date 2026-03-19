package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class AdjustPointsRequest {
    @SerializedName("points")
    private int points; // Dương để cộng, âm để trừ

    @SerializedName("reason")
    private String reason;

    public AdjustPointsRequest(int points, String reason) {
        this.points = points;
        this.reason = reason;
    }

    public int getPoints() { return points; }
    public String getReason() { return reason; }
}
