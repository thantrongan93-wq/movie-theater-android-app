package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {
    @SerializedName("token")
    private String token;

    public LogoutRequest(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
