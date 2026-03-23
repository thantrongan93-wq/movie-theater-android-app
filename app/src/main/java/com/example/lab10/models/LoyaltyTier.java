package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class LoyaltyTier {
    @SerializedName("id")
    private Long id;

    @SerializedName("tierName")
    private String tierName;

    @SerializedName("minPoints")
    private Integer minPoints;

    @SerializedName("maxPoints")
    private Integer maxPoints;

    @SerializedName("benefits")
    private String benefits;

    public Long getId() { return id; }
    public String getTierName() { return tierName; }
    public Integer getMinPoints() { return minPoints; }
    public Integer getMaxPoints() { return maxPoints; }
    public String getBenefits() { return benefits; }
}
