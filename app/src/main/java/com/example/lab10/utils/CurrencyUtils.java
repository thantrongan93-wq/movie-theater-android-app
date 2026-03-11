package com.example.lab10.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    
    public static String formatPrice(Double price) {
        if (price == null) return "";
        return String.format(Locale.getDefault(), "%.0f VNĐ", price);
    }
}
