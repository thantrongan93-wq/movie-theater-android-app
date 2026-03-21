package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.PaymentResponse;
import com.example.lab10.utils.CurrencyUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "extra_booking";

    private TextView tvBookingId, tvMovieTitle, tvTotal;
    private Button btnConfirm;
    private ProgressBar pbLoading;
    private Booking booking;
    private MovieApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        apiService = ApiClient.getApiService();
        booking = (Booking) getIntent().getSerializableExtra(EXTRA_BOOKING);
        
        initViews();
        
        if (booking != null) {
            displayInfo();
        } else {
            tvBookingId.setText("Giao dịch gần nhất");
            tvMovieTitle.setText("Hệ thống tự động xác định");
            tvTotal.setText("Theo hóa đơn");
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvBookingId = findViewById(R.id.tv_payment_booking_id);
        tvMovieTitle = findViewById(R.id.tv_payment_movie_title);
        tvTotal = findViewById(R.id.tv_payment_total);
        btnConfirm = findViewById(R.id.btn_confirm_payment);
        pbLoading = findViewById(R.id.pb_payment_loading);

        btnConfirm.setOnClickListener(v -> startVNPayPayment());
    }

    private void displayInfo() {
        String code = booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
        tvBookingId.setText(code);
        if (booking.getShowtime() != null && booking.getShowtime().getMovie() != null) {
            tvMovieTitle.setText(booking.getShowtime().getMovie().getTitle());
        }
        tvTotal.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
    }

    private void startVNPayPayment() {
        setLoading(true);

        apiService.createVNPayUrlAuto().enqueue(new Callback<ApiResponse<PaymentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentResponse>> call, Response<ApiResponse<PaymentResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    String url = response.body().getResult().getVnpayUrl();
                    if (url != null && !url.isEmpty()) {
                        // Mở PaymentResultActivity (Trang chứa WebView) thay vì trình duyệt Chrome
                        Intent intent = new Intent(PaymentActivity.this, PaymentResultActivity.class);
                        intent.putExtra("PAYMENT_URL", url);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Không nhận được liên kết thanh toán", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Lỗi khởi tạo thanh toán", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirm.setEnabled(!isLoading);
        btnConfirm.setText(isLoading ? "Đang xử lý..." : "Thanh toán ngay");
    }
}
