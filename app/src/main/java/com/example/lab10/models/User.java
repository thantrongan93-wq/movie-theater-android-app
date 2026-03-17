package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

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

    @SerializedName(value = "phone", alternate = {"phoneNumber"})
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("password")
    private String password;

    @SerializedName("confirmPassword")
    private String confirmPassword;

    @SerializedName("gender")
    private String gender; // "MALE" or "FEMALE"

    @SerializedName("roles")
    private List<String> roles;

    public User() {}

    public Long getId() { return userId != null ? userId : id; }
    public void setId(Long id) { this.id = id; this.userId = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    /** Kiểm tra xem user có phải admin không */
    public boolean isAdmin() {
        if (roles == null) return false;
        for (String r : roles) {
            if (r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN")) return true;
        }
        return false;
    }

    // Các getters/setters khác giữ nguyên để đảm bảo tương thích
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
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
