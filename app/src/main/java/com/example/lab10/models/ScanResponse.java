package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ScanResponse implements Serializable {
    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("movieName")
    private String movieName;

    @SerializedName("showtime")
    private String showtime;

    @SerializedName("room")
    private String room;

    @SerializedName("seats")
    private List<String> seats;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("totalPrice")
    private Double totalPrice;

    @SerializedName("status")
    private String status; // "PENDING", "CONFIRMED", "CHECKED_IN", "CANCELLED"

    @SerializedName("isCheckedIn")
    private Boolean isCheckedIn;

    @SerializedName("checkedInTime")
    private String checkedInTime;

    @SerializedName("foodItems")
    private List<FoodItem> foodItems;

    public ScanResponse() {}

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }

    public String getShowtime() { return showtime; }
    public void setShowtime(String showtime) { this.showtime = showtime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsCheckedIn() { return isCheckedIn; }
    public void setIsCheckedIn(Boolean isCheckedIn) { this.isCheckedIn = isCheckedIn; }

    public String getCheckedInTime() { return checkedInTime; }
    public void setCheckedInTime(String checkedInTime) { this.checkedInTime = checkedInTime; }

    public List<FoodItem> getFoodItems() { return foodItems; }
    public void setFoodItems(List<FoodItem> foodItems) { this.foodItems = foodItems; }
}
