package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Theater implements Serializable {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("totalSeats")
    private Integer totalSeats;

    // Constructors
    public Theater() {
    }

    public Theater(Long id, String name, String location, String address, Integer totalSeats) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.address = address;
        this.totalSeats = totalSeats;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}
