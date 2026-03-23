package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScanResponse implements Serializable {
    @SerializedName("tickets")
    private List<TicketInfo> tickets;

    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName(value = "customerName", alternate = {"customer", "username", "userName"})
    private String customerName;

    @SerializedName(value = "phoneNumber", alternate = {"phone"})
    private String phoneNumber;

    @SerializedName(value = "movieName", alternate = {"movie", "movieTitle"})
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

    @SerializedName(value = "status", alternate = {"bookingStatus"})
    private String status; // "PENDING", "CONFIRMED", "CHECKED_IN", "CANCELLED"

    @SerializedName("isCheckedIn")
    private Boolean isCheckedIn;

    @SerializedName("checkedInTime")
    private String checkedInTime;

    @SerializedName("ticketsPrinted")
    private Integer ticketsPrinted;

    @SerializedName("foodItems")
    private List<FoodItem> foodItems;

    public ScanResponse() {}

    public String getBookingId() {
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            return bookingId;
        }
        TicketInfo ticket = getPrimaryTicket();
        return ticket != null ? ticket.getBookingId() : null;
    }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerName() {
        if (customerName != null && !customerName.trim().isEmpty()) {
            return customerName;
        }
        TicketInfo ticket = getPrimaryTicket();
        return ticket != null ? ticket.getCustomerName() : null;
    }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMovieName() {
        if (movieName != null && !movieName.trim().isEmpty()) {
            return movieName;
        }
        TicketInfo ticket = getPrimaryTicket();
        return ticket != null ? ticket.getMovieTitle() : null;
    }
    public void setMovieName(String movieName) { this.movieName = movieName; }

    public String getShowtime() {
        if (showtime != null && !showtime.trim().isEmpty()) {
            return showtime;
        }
        TicketInfo ticket = getPrimaryTicket();
        return ticket != null ? ticket.getShowtime() : null;
    }
    public void setShowtime(String showtime) { this.showtime = showtime; }

    public String getRoom() {
        if (room != null && !room.trim().isEmpty()) {
            return room;
        }
        TicketInfo ticket = getPrimaryTicket();
        return ticket != null ? ticket.getRoom() : null;
    }
    public void setRoom(String room) { this.room = room; }

    public List<String> getSeats() {
        if (seats != null && !seats.isEmpty()) {
            return seats;
        }
        TicketInfo ticket = getPrimaryTicket();
        if (ticket == null || ticket.getSeatNumber() == null || ticket.getSeatNumber().trim().isEmpty()) {
            return null;
        }
        List<String> mappedSeats = new ArrayList<>();
        mappedSeats.add(ticket.getSeatNumber().trim());
        return mappedSeats;
    }
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

    public Integer getTicketsPrinted() { return ticketsPrinted; }
    public void setTicketsPrinted(Integer ticketsPrinted) { this.ticketsPrinted = ticketsPrinted; }

    public List<TicketInfo> getTickets() { return tickets; }
    public void setTickets(List<TicketInfo> tickets) { this.tickets = tickets; }

    public List<FoodItem> getFoodItems() { return foodItems; }
    public void setFoodItems(List<FoodItem> foodItems) { this.foodItems = foodItems; }

    public TicketInfo getPrimaryTicket() {
        return tickets != null && !tickets.isEmpty() ? tickets.get(0) : null;
    }

    public static class TicketInfo implements Serializable {
        @SerializedName("showtime")
        private String showtime;

        @SerializedName("movieTitle")
        private String movieTitle;

        @SerializedName("seatNumber")
        private String seatNumber;

        @SerializedName("customerName")
        private String customerName;

        @SerializedName("room")
        private String room;

        @SerializedName("bookingId")
        private String bookingId;

        public String getShowtime() { return showtime; }
        public String getMovieTitle() { return movieTitle; }
        public String getSeatNumber() { return seatNumber; }
        public String getCustomerName() { return customerName; }
        public String getRoom() { return room; }
        public String getBookingId() { return bookingId; }
    }
}
