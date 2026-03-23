package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LoyaltyInfo implements Serializable {

    @SerializedName(value = "currentPoints", alternate = {"points", "totalPoints", "point", "current_points"})
    private Integer currentPoints;

    @SerializedName(value = "tierName", alternate = {"tier", "loyaltyTier", "tier_name"})
    private String tierName;

    @SerializedName(value = "discountPercentage", alternate = {"discount", "discountPercent"})
    private Double discountPercentage;

    @SerializedName(value = "userId", alternate = {"user_id"})
    private Long userId;

    @SerializedName(value = "nextTierPoints", alternate = {"pointsToNextTier", "next_tier_points"})
    private Integer nextTierPoints;

    public Integer getCurrentPoints() { return currentPoints != null ? currentPoints : 0; }
    public void setCurrentPoints(Integer p) { this.currentPoints = p; }

    public String getTierName() { return tierName; }
    public void setTierName(String t) { this.tierName = t; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double d) { this.discountPercentage = d; }

    public Long getUserId() { return userId; }
    public void setUserId(Long u) { this.userId = u; }

    public Integer getNextTierPoints() { return nextTierPoints; }
    public void setNextTierPoints(Integer n) { this.nextTierPoints = n; }
}