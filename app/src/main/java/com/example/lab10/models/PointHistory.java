package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class PointHistory {
    @SerializedName("id")
    private Long id;

    @SerializedName("type")
    private String type;

    @SerializedName("amount")
    private Integer amount;

    @SerializedName("dateTime")
    private String dateTime;

    // Getters
    public Long getId() { return id; }
    public String getType() { return type; }
    public Integer getAmount() { return amount; }
    public String getDateTime() { return dateTime; }
}
