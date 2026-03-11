package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Room implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("capacity")
    private Integer capacity;

    @SerializedName("status")
    private String status;

    @SerializedName("theaterId")
    private Long theaterId;

    @SerializedName("theater")
    private Theater theater;

    public Room() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }

    public Theater getTheater() { return theater; }
    public void setTheater(Theater theater) { this.theater = theater; }
}
