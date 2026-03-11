package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    // New API trả về "statusCode", old API trả về "code"
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("result")
    private T result;

    public ApiResponse() {}

    public int getCode() {
        // Trả về giá trị nào khác 0 (statusCode ưu tiên)
        return statusCode != 0 ? statusCode : code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** Trả về data từ field "data" hoặc "result" */
    public T getResult() {
        return data != null ? data : result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isSuccess() {
        int c = getCode();
        return c == 200 || c == 201 || c == 1000 || getResult() != null;
    }
}
