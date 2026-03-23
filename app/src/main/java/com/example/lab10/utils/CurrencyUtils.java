package com.example.lab10.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    private static final Locale VI_LOCALE = new Locale("vi", "VN");

    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VI_LOCALE);
        return formatter.format(amount);
    }
    
    public static String formatPrice(Double price) {
        if (price == null) return "0 VNĐ";
        NumberFormat formatter = NumberFormat.getInstance(VI_LOCALE);
        return formatter.format(price) + " VNĐ";
    }
}
