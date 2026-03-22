package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class PaymentStatusResponse {
    @SerializedName("status")
    private String status; // PENDING | PAID | CANCELLED

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(status);
    }
}
