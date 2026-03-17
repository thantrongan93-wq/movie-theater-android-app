package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ShowtimeGroup implements Serializable {
    @SerializedName("date")
    private String date;

    @SerializedName("showtimes")
    private List<ShowtimeInfo> showtimes;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<ShowtimeInfo> getShowtimes() { return showtimes; }
    public void setShowtimes(List<ShowtimeInfo> showtimes) { this.showtimes = showtimes; }

    public static class ShowtimeInfo implements Serializable {
        @SerializedName("time")
        private String time;

        @SerializedName("showtimeId")
        private Long showtimeId;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public Long getShowtimeId() { return showtimeId; }
        public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    }
}
