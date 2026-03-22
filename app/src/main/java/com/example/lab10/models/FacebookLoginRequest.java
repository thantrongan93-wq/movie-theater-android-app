package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class FacebookLoginRequest {
    @SerializedName("accessToken")
    private String accessToken;

    public FacebookLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
