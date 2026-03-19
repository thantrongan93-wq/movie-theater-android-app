package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class PointHistory {
    @SerializedName("id")
    private String id;

    @SerializedName("points")
    private int points;

    @SerializedName("reason")
    private String reason;

    @SerializedName("date")
    private String date;

    @SerializedName("balance")
    private int balance;

    public PointHistory(String id, int points, String reason, String date, int balance) {
        this.id = id;
        this.points = points;
        this.reason = reason;
        this.date = date;
        this.balance = balance;
    }

    // Getters
    public String getId() { return id; }
    public int getPoints() { return points; }
    public String getReason() { return reason; }
    public String getDate() { return date; }
    public int getBalance() { return balance; }
}