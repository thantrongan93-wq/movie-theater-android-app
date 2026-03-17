package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class BookingHistoryResponse implements Serializable {
    @SerializedName("bookingId")
    private String bookingId;
    
    @SerializedName("movieId")
    private Long movieId;
    
    @SerializedName("showtimeDetailId")
    private Long showtimeDetailId;
    
    @SerializedName("cinemaRoomId")
    private Long cinemaRoomId;
    
    @SerializedName("seatNumbers")
    private List<String> seatNumbers;
    
    @SerializedName("foods")
    private List<BookingFood> foods;
    
    @SerializedName("totalPrice")
    private Double totalPrice;
    
    @SerializedName("bookingDate")
    private String bookingDate;
    
    @SerializedName("status")
    private String status;

    // Transient fields to store additional info fetched later
    private transient Movie movie;
    private transient Showtime showtime;

    public static class BookingFood implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("quantity")
        private Integer quantity;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public void setShowtimeDetailId(Long showtimeDetailId) { this.showtimeDetailId = showtimeDetailId; }
    public Long getCinemaRoomId() { return cinemaRoomId; }
    public void setCinemaRoomId(Long cinemaRoomId) { this.cinemaRoomId = cinemaRoomId; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }
    public List<BookingFood> getFoods() { return foods; }
    public void setFoods(List<BookingFood> foods) { this.foods = foods; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public Showtime getShowtime() { return showtime; }
    public void setShowtime(Showtime showtime) { this.showtime = showtime; }
}
