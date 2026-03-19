package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class LoyaltyInfo {
    @SerializedName("points")
    private int points;

    @SerializedName("tier")
    private String tier;

    @SerializedName("nextTier")
    private String nextTier;

    @SerializedName("pointsToNextTier")
    private int pointsToNextTier;

    @SerializedName("totalSpent")
    private double totalSpent;

    // Constructor
    public LoyaltyInfo(int points, String tier, String nextTier,
                       int pointsToNextTier, double totalSpent) {
        this.points = points;
        this.tier = tier;
        this.nextTier = nextTier;
        this.pointsToNextTier = pointsToNextTier;
        this.totalSpent = totalSpent;
    }

    // Getters
    public int getPoints() { return points; }
    public String getTier() { return tier; }
    public String getNextTier() { return nextTier; }
    public int getPointsToNextTier() { return pointsToNextTier; }
    public double getTotalSpent() { return totalSpent; }
}