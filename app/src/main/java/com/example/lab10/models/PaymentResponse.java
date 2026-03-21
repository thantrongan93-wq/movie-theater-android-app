package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PaymentResponse implements Serializable {
    @SerializedName("vnpayUrl")
    private String vnpayUrl;
    
    @SerializedName("expiryTime")
    private String expiryTime;
    
    @SerializedName("remainingMinutes")
    private Integer remainingMinutes;

    public String getVnpayUrl() { return vnpayUrl; }
    public void setVnpayUrl(String vnpayUrl) { this.vnpayUrl = vnpayUrl; }
    public String getExpiryTime() { return expiryTime; }
    public void setExpiryTime(String expiryTime) { this.expiryTime = expiryTime; }
    public Integer getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(Integer remainingMinutes) { this.remainingMinutes = remainingMinutes; }
}
