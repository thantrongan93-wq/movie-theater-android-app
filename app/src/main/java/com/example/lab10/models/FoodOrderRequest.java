package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodOrderRequest {

    @SerializedName("foodItems")
    private List<FoodItemOrder> foodItems;

    @SerializedName("foodCombos")
    private List<FoodComboOrder> foodCombos;

    public FoodOrderRequest(List<FoodItemOrder> foodItems, List<FoodComboOrder> foodCombos) {
        this.foodItems = foodItems;
        this.foodCombos = foodCombos;
    }

    public static class FoodItemOrder {
        @SerializedName("foodItemId")
        private Long foodItemId;

        @SerializedName("quantity")
        private Integer quantity;

        public FoodItemOrder(Long foodItemId, Integer quantity) {
            this.foodItemId = foodItemId;
            this.quantity = quantity;
        }
    }

    public static class FoodComboOrder {
        @SerializedName("comboId")
        private Long comboId;

        @SerializedName("quantity")
        private Integer quantity;

        public FoodComboOrder(Long comboId, Integer quantity) {
            this.comboId = comboId;
            this.quantity = quantity;
        }
    }
}