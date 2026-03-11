package com.example.lab10.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.lab10.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "MovieBookingSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_TOKEN = "jwtToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    /** Lưu session sau khi login thành công */
    public void createLoginSession(User user, String token) {
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean(KEY_IS_LOGGED_IN, true);
        if (user != null) {
            ed.putLong(KEY_USER_ID, user.getId() != null ? user.getId() : -1);
            ed.putString(KEY_USERNAME, user.getUsername());
            ed.putString(KEY_EMAIL, user.getEmail());
            ed.putString(KEY_FULL_NAME, user.getFullName());
            ed.putString(KEY_USER, gson.toJson(user));
        }
        ed.putString(KEY_TOKEN, token);
        ed.commit();
    }

    /** Lưu session chỉ với token (khi chưa có thông tin user) */
    public void createLoginSession(String token) {
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean(KEY_IS_LOGGED_IN, true);
        ed.putString(KEY_TOKEN, token);
        ed.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor ed = pref.edit();
        ed.putLong(KEY_USER_ID, user.getId() != null ? user.getId() : -1);
        ed.putString(KEY_USERNAME, user.getUsername());
        ed.putString(KEY_EMAIL, user.getEmail());
        ed.putString(KEY_FULL_NAME, user.getFullName());
        ed.putString(KEY_USER, gson.toJson(user));
        ed.apply();
    }

    public Long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, null);
    }

    /** Lấy JWT token */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    /** Lưu JWT token */
    public void saveToken(String token) {
        pref.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return pref.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveRefreshToken(String refreshToken) {
        pref.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    public void logout() {
        pref.edit().clear().commit();
    }
}
