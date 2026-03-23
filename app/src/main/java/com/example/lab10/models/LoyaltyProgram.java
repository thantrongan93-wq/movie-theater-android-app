package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class LoyaltyProgram {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
