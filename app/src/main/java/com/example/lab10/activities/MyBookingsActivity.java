package com.example.lab10.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.BookingAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {
    
    private ImageView ivBack;
    private RecyclerView rvBookings;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private BookingAdapter bookingAdapter;
    private SessionManager sessionManager;
    private MovieApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        sessionManager = new SessionManager(this);
        
        // Khôi phục JWT token nếu bị mất
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }
        
        apiService = ApiClient.getApiService();
        
        initViews();
        loadBookings();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvBookings = findViewById(R.id.rv_bookings);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        
        ivBack.setOnClickListener(v -> finish());
        
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(new ArrayList<>(), this::onCancelBooking);
        rvBookings.setAdapter(bookingAdapter);
    }
    
    private void loadBookings() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        Log.d("BOOKINGS", "Loading my bookings...");
        
        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("BOOKINGS", "HTTP code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    Log.d("BOOKINGS", "api code: " + apiResponse.getCode() + " message: " + apiResponse.getMessage());
                    
                    List<Booking> bookings = apiResponse.getResult();
                    Log.d("BOOKINGS", "bookings size: " + (bookings != null ? bookings.size() : 0));
                    
                    if (bookings != null && !bookings.isEmpty()) {
                        bookingAdapter.updateBookings(bookings);
                        rvBookings.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        rvBookings.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Bạn chưa có đặt vé nào");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("BOOKINGS", "Error " + response.code() + ": " + errorBody);
                        
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Lỗi tải lịch sử đặt vé");
                        Toast.makeText(MyBookingsActivity.this, "Không thể tải lịch sử đặt vé", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("BOOKINGS", "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Lỗi kết nối");
                
                Log.e("BOOKINGS", "onFailure: " + t.getMessage(), t);
                Toast.makeText(MyBookingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings("unused")
    private void onCancelBooking(Booking booking) {
        Toast.makeText(this, "Chức năng hủy vé chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
    }
}
