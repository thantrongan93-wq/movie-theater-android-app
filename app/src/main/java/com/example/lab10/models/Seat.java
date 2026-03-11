package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Seat implements Serializable {
    @SerializedName("seatId")
    private Long id;

    // Fallback nếu API trả về "id" thay vì "seatId"
    @SerializedName("id")
    private Long idAlt;
    
    @SerializedName("theaterId")
    private Long theaterId;
    
    @SerializedName("seatNumber")
    private String seatNumber;
    
    @SerializedName("rowNumber")
    private String rowNumber;
    
    @SerializedName("seatType")
    private String seatType; // VIP, Regular, etc.
    
    @SerializedName("isAvailable")
    private Boolean isAvailable;

    @SerializedName("status")
    private String status; // AVAILABLE, BOOKED, SELECTED, etc.

    @SerializedName("showtimeId")
    private Long showtimeId;

    @SerializedName("seatTypeEnum")
    private String seatTypeEnum;

    // Local properties for UI
    private boolean isSelected = false;

    // Constructors
    public Seat() {
    }

    public Seat(Long id, Long theaterId, String seatNumber, String rowNumber, 
                String seatType, Boolean isAvailable, Long showtimeId) {
        this.id = id;
        this.theaterId = theaterId;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.seatType = seatType;
        this.isAvailable = isAvailable;
        this.showtimeId = showtimeId;
    }

    // Getters and Setters
    public Long getId() {
        return id != null ? id : idAlt;
    }

    public void setId(Long id) {
        this.id = id;
        this.idAlt = id;
    }

    public Long getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(Long theaterId) {
        this.theaterId = theaterId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public Boolean getIsAvailable() {
        // Hỗ trợ cả field isAvailable và status
        if (isAvailable != null) return isAvailable;
        return status == null || "AVAILABLE".equalsIgnoreCase(status);
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
