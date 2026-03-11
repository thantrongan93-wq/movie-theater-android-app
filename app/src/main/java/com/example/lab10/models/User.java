package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("userId")
    private Long userId;

    @SerializedName("id")
    private Long id;

    @SerializedName("username")
    private String username;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("email")
    private String email;

    @SerializedName("identityCard")
    private String identityCard;

    // New API dùng "phone", old API dùng "phoneNumber" — hỗ trợ cả hai
    @SerializedName(value = "phone", alternate = {"phoneNumber"})
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("password")
    private String password;

    // Chỉ dùng khi đăng ký — không có trong response
    @SerializedName("confirmPassword")
    private String confirmPassword;

    @SerializedName("gender")
    private String gender; // "MALE" or "FEMALE"

    public User() {}

    // Getters and Setters
    public Long getId() { return userId != null ? userId : id; }
    public void setId(Long id) { this.id = id; this.userId = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
