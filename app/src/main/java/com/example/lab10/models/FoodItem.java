package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FoodItem implements Serializable {

    @SerializedName("foodItemId")
    private Long foodItemId;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private Double price;

    @SerializedName("quantityAvailable")
    private Integer quantityAvailable;

    @SerializedName("purchaseDate")
    private String purchaseDate;

    @SerializedName("purchasePrice")
    private Double purchasePrice;

    @SerializedName("imageUrl")
    private String imageUrl;

    // transient — số lượng user chọn, không từ API
    private transient int quantity = 0;

    public Long getFoodItemId() { return foodItemId; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Integer getQuantityAvailable() { return quantityAvailable; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}