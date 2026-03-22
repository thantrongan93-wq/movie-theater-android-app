package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FoodCombo implements Serializable {

    @SerializedName("comboId")
    private Long comboId;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private Double price;

    @SerializedName("limitedQuantity")
    private Boolean limitedQuantity;

    @SerializedName("quantityAvailable")
    private Integer quantityAvailable;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    // transient — số lượng user chọn, không từ API
    private transient int quantity = 0;

    public Long getComboId() { return comboId; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Boolean getLimitedQuantity() { return limitedQuantity; }
    public Integer getQuantityAvailable() { return quantityAvailable; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}