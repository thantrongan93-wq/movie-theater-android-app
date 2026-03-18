package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Showtime implements Serializable {
    // showtime-details endpoint trả về showtimeDetailId (dùng cho booking và seat)
    @SerializedName("showtimeDetailId")
    private Long showtimeDetailId;

    @SerializedName("showtimeId")
    private Long id;
    
    @SerializedName("movieId")
    private Long movieId;
    
    @SerializedName("movie")
    private Movie movie;
    
    @SerializedName("theaterId")
    private Long theaterId;
    
    @SerializedName("theater")
    private Theater theater;
    
    @SerializedName("showDate")
    private String showDate;
    
    @SerializedName("showTime")
    private String showTime;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("availableSeats")
    private Integer availableSeats;

    @SerializedName("roomId")
    private Long roomId;

    @SerializedName("cinemaRoomId")
    private Long cinemaRoomId;

    @SerializedName("cinemaRoomName")
    private String cinemaRoomName;

    @SerializedName("room")
    private Room room;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("status")
    private String status;

    // Constructors
    public Showtime() {
    }

    public Showtime(Long id, Long movieId, Movie movie, Long theaterId, 
                    Theater theater, String showDate, String showTime, 
                    Double price, Integer availableSeats) {
        this.id = id;
        this.movieId = movieId;
        this.movie = movie;
        this.theaterId = theaterId;
        this.theater = theater;
        this.showDate = showDate;
        this.showTime = showTime;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    /** Trả về showtimeDetailId nếu có (từ endpoint showtime-details), ngược lại showtimeId */
    public Long getId() {
        return showtimeDetailId != null ? showtimeDetailId : id;
    }

    public Long getShowtimeDetailId() { return showtimeDetailId; }
    public void setShowtimeDetailId(Long showtimeDetailId) { this.showtimeDetailId = showtimeDetailId; }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Long getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(Long theaterId) {
        this.theaterId = theaterId;
    }

    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public Double getPrice() {
        return price != null ? price : basePrice;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Long getRoomId() {
        return roomId != null ? roomId : cinemaRoomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getCinemaRoomId() {
        return cinemaRoomId;
    }

    public void setCinemaRoomId(Long cinemaRoomId) {
        this.cinemaRoomId = cinemaRoomId;
    }

    public String getCinemaRoomName() {
        return cinemaRoomName;
    }

    public void setCinemaRoomName(String cinemaRoomName) {
        this.cinemaRoomName = cinemaRoomName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getStartTime() {
        return startTime != null ? startTime : showTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("basePrice")
    private Double basePrice;
}
