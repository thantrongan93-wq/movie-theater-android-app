package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {
    @SerializedName("idToken")
    private String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
