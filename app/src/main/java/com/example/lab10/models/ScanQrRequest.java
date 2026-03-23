package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class ScanQrRequest {
    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("qrData")
    private String qrData;

    public ScanQrRequest(String rawQrContent) {
        this.qrCode = rawQrContent;
        this.qrData = rawQrContent;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }
}
