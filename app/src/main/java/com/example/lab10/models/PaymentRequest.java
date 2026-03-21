package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("bookingId")
    private String bookingId;

    public PaymentRequest(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}
