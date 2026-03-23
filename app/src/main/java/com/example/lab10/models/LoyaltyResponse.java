package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class LoyaltyResponse {
    @SerializedName(value = "totalPoints", alternate = {"loyaltyPoints", "currentPoints"})
    private Integer totalPoints;
    
    @SerializedName(value = "tierName", alternate = {"currentTierName"})
    private String tierName;
    
    @SerializedName(value = "nextTierPoints", alternate = {"pointsToNextTier"})
    private Integer nextTierPoints;

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public Integer getNextTierPoints() { return nextTierPoints; }
    public void setNextTierPoints(Integer nextTierPoints) { this.nextTierPoints = nextTierPoints; }
}
