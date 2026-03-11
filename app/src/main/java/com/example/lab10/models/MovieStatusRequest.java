package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class MovieStatusRequest {
    @SerializedName("status")
    private String status; // NOW_SHOWING, COMING_SOON, etc.

    public MovieStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
