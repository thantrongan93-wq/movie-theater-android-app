package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho các API dashboard admin.
 * Gửi nhiều alias field để tương thích các naming convention ở backend.
 */
public class AdminReportRequest {
    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("toDate")
    private String toDate;

    public AdminReportRequest(Integer month, Integer year, String startDate, String endDate) {
        this.month = month;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fromDate = startDate;
        this.toDate = endDate;
    }

    public AdminReportRequest(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.fromDate = startDate;
        this.toDate = endDate;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
