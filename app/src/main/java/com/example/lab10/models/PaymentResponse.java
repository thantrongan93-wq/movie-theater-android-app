package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PaymentResponse implements Serializable {
    @SerializedName("totalPrice")
    private Double totalPrice;
    
    @SerializedName("expiryTime")
    private String expiryTime;
    
    @SerializedName("bookingId")
    private String bookingId;
    
    @SerializedName("qrData")
    private QrData qrData;
    
    @SerializedName("remainingMinutes")
    private Integer remainingMinutes;

    // Các field cũ cho VNPay (nếu vẫn cần dùng song song)
    @SerializedName("vnpayUrl")
    private String vnpayUrl;

    public Double getTotalPrice() { return totalPrice; }
    public String getExpiryTime() { return expiryTime; }
    public String getBookingId() { return bookingId; }
    public QrData getQrData() { return qrData; }
    public Integer getRemainingMinutes() { return remainingMinutes; }
    public String getVnpayUrl() { return vnpayUrl; }

    public static class QrData implements Serializable {
        private String bankCode;
        private String bankName;
        private String bankAccount;
        private String userBankName;
        private String amount;
        private String content;
        private String qrCode;
        private String qrLink;
        private String qrDataURL;

        public String getBankCode() { return bankCode; }
        public String getBankName() { return bankName; }
        public String getBankAccount() { return bankAccount; }
        public String getUserBankName() { return userBankName; }
        public String getAmount() { return amount; }
        public String getContent() { return content; }
        public String getQrCode() { return qrCode; }
        public String getQrLink() { return qrLink; }
        public String getQrDataURL() { return qrDataURL; }
    }
}
