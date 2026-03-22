package com.example.lab10.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
        apiService = ApiClient.getApiService();
        
        initViews();
        loadBookings();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vé của tôi");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBookings = findViewById(R.id.rv_bookings);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(new ArrayList<>(), this::onCancelBooking,
                this::showBookingDetail);
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
        
        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getResult();
                    if (bookings != null && !bookings.isEmpty()) {
                        bookingAdapter.updateData(bookings);
                        rvBookings.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e("MY_BOOKINGS", "Error: " + t.getMessage());
            }
        });
    }

    private void onCancelBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.cancelPendingBooking().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(MyBookingsActivity.this,
                            "Đã hủy vé thành công", Toast.LENGTH_SHORT).show();
                    loadBookings();
                } else {
                    Toast.makeText(MyBookingsActivity.this,
                            "Không thể hủy vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyBookingsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showBookingDetail(Booking booking) {
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đặt vé");

        // Build message
        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(
                        booking.getBookingUuid() != null ? booking.getBookingUuid()
                                : booking.getBookingCode() != null ? booking.getBookingCode()
                                : booking.getId() != null ? "#" + booking.getId() : "N/A")
                .append("\n");

        if (booking.getShowtime() != null) {
            if (booking.getShowtime().getMovie() != null) {
                sb.append("Phim: ").append(booking.getShowtime().getMovie().getTitle()).append("\n");
            }
            if (booking.getShowtime().getStartTime() != null) {
                sb.append("Suất: ").append(
                        com.example.lab10.utils.DateTimeUtils.formatTime(
                                booking.getShowtime().getStartTime())).append("\n");
            }
            if (booking.getShowtime().getCinemaRoomName() != null) {
                sb.append("Phòng: ").append(booking.getShowtime().getCinemaRoomName()).append("\n");
            }
        }

        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            String seats = booking.getSeats().stream()
                    .map(s -> (s.getRowNumber() != null ? s.getRowNumber() : "")
                            + (s.getSeatNumber() != null ? s.getSeatNumber() : ""))
                    .collect(java.util.stream.Collectors.joining(", "));
            sb.append("Ghế: ").append(seats).append("\n");
        }

        sb.append("Tổng tiền: ").append(
                        com.example.lab10.utils.CurrencyUtils.formatPrice(
                                booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0))
                .append("\n");
        sb.append("Trạng thái: ").append(
                booking.getStatus() != null ? booking.getStatus() : "PENDING");

        builder.setMessage(sb.toString());

        // Chỉ show confirm nếu PENDING
        boolean isPending = "PENDING".equals(booking.getStatus())
                || "BOOKING".equals(booking.getStatus());

        if (isPending) {
            builder.setPositiveButton("Xác nhận", (dialog, which) ->
                    showConfirmDialog(booking));
            builder.setNeutralButton("Hủy vé", (dialog, which) ->
                    onCancelBooking(booking));
        }

        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void showConfirmDialog(Booking booking) {
        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_confirm_booking, null);

        android.widget.EditText etPhone     = dialogView.findViewById(R.id.et_phone);
        android.widget.EditText etPromoId   = dialogView.findViewById(R.id.et_promotion_id);
        android.widget.EditText etCoupon    = dialogView.findViewById(R.id.et_coupon_code);
        android.widget.EditText etPoints    = dialogView.findViewById(R.id.et_points);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt vé")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String phone     = etPhone.getText().toString().trim();
                    String promoStr  = etPromoId.getText().toString().trim();
                    String coupon    = etCoupon.getText().toString().trim();
                    String pointsStr = etPoints.getText().toString().trim();

                    Long promotionId  = promoStr.isEmpty() ? null : Long.parseLong(promoStr);
                    Integer points    = pointsStr.isEmpty() ? null : Integer.parseInt(pointsStr);
                    String phoneVal   = phone.isEmpty() ? null : phone;
                    String couponVal  = coupon.isEmpty() ? null : coupon;

                    confirmBooking(phoneVal, promotionId, couponVal, points);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmBooking(String phone, Long promotionId,
                                String couponCode, Integer pointsToUse) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.confirmBookingWithParams(phone, promotionId, couponCode, pointsToUse)
                .enqueue(new Callback<ApiResponse<Booking>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Booking>> call,
                                           Response<ApiResponse<Booking>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(MyBookingsActivity.this,
                                    "Xác nhận thành công!", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "";
                                Toast.makeText(MyBookingsActivity.this,
                                        "Xác nhận thất bại: " + err,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception ignored) {}
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyBookingsActivity.this,
                                "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
