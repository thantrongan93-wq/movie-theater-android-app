package com.example.lab10.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.BookingHistoryAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.BookingHistoryResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryActivity extends AppCompatActivity {

    private static final String TAG = "BookingHistoryActivity";
    private RecyclerView rvBookingHistory;
    private ProgressBar pbLoading;
    private LinearLayout llEmptyState;
    private BookingHistoryAdapter adapter;
    private MovieApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        initViews();
        setupToolbar();
        
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        loadBookingHistory();
    }

    private void initViews() {
        rvBookingHistory = findViewById(R.id.rv_booking_history);
        pbLoading = findViewById(R.id.pb_loading);
        llEmptyState = findViewById(R.id.ll_empty_state);

        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingHistoryAdapter(new ArrayList<>());
        rvBookingHistory.setAdapter(adapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadBookingHistory() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        rvBookingHistory.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        apiService.getMyBookingHistory().enqueue(new Callback<ApiResponse<List<BookingHistoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingHistoryResponse>>> call, Response<ApiResponse<List<BookingHistoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingHistoryResponse> bookings = response.body().getResult();
                    if (bookings != null && !bookings.isEmpty()) {
                        fetchMovieDetails(bookings);
                    } else {
                        pbLoading.setVisibility(View.GONE);
                        llEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(BookingHistoryActivity.this, "Không thể tải lịch sử đặt vé", Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingHistoryResponse>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(BookingHistoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovieDetails(List<BookingHistoryResponse> bookings) {
        final int totalToFetch = bookings.size();
        final int[] fetchedCount = {0};

        if (totalToFetch == 0) {
            pbLoading.setVisibility(View.GONE);
            adapter.updateData(bookings);
            rvBookingHistory.setVisibility(View.VISIBLE);
            return;
        }

        for (BookingHistoryResponse booking : bookings) {
            if (booking.getMovieId() != null) {
                apiService.getMovieById(booking.getMovieId()).enqueue(new Callback<ApiResponse<Movie>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            booking.setMovie(response.body().getResult());
                        }
                        checkProgress();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                        checkProgress();
                    }

                    private void checkProgress() {
                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalToFetch) {
                            pbLoading.setVisibility(View.GONE);
                            adapter.updateData(bookings);
                            rvBookingHistory.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } else {
                fetchedCount[0]++;
                if (fetchedCount[0] == totalToFetch) {
                    pbLoading.setVisibility(View.GONE);
                    adapter.updateData(bookings);
                    rvBookingHistory.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
