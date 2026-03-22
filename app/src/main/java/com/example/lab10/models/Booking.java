package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Booking implements Serializable {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("user")
    private User user;
    
    @SerializedName("showtimeId")
    private Long showtimeId;
    
    @SerializedName("showtime")
    private Showtime showtime;
    
    @SerializedName("seats")
    private List<Seat> seats;
    
    @SerializedName("seatIds")
    private List<Long> seatIds;
    
    @SerializedName("totalPrice")
    private Double totalPrice;
    
    @SerializedName("bookingDate")
    private String bookingDate;
    
    @SerializedName("status")
    private String status; // CONFIRMED, CANCELLED, PENDING
    
    @SerializedName("bookingId") // Ánh xạ từ backend
    private String bookingCode;
    @SerializedName("bookingId")
    private String bookingUuid;

    @SerializedName("confirmExpiryTime")
    private String confirmExpiryTime;

    @SerializedName("remainingMinutes")
    private Integer remainingMinutes;

    // Constructors
    public Booking() {
    }

    public Booking(Long id, Long userId, User user, Long showtimeId, 
                   Showtime showtime, List<Seat> seats, List<Long> seatIds,
                   Double totalPrice, String bookingDate, String status, String bookingCode) {
        this.id = id;
        this.userId = userId;
        this.user = user;
        this.showtimeId = showtimeId;
        this.showtime = showtime;
        this.seats = seats;
        this.seatIds = seatIds;
        this.totalPrice = totalPrice;
        this.bookingDate = bookingDate;
        this.status = status;
        this.bookingCode = bookingCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
    public String getBookingUuid() { return bookingUuid; }
    public String getConfirmExpiryTime() { return confirmExpiryTime; }
    public Integer getRemainingMinutes() { return remainingMinutes; }
}
