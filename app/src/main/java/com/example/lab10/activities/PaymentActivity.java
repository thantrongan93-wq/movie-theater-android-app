package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.PaymentRequest;
import com.example.lab10.models.PaymentResponse;
import com.example.lab10.models.PaymentStatusResponse;
import com.example.lab10.utils.CurrencyUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "extra_booking";
    private static final String TAG = "PaymentActivity";

    private TextView tvBookingId, tvMovieTitle, tvTotal, tvExpiry, tvBankInfo;
    private Button btnConfirm;
    private ProgressBar pbLoading;
    private ImageView ivQrCode;
    private CardView cvQrContainer;
    private Booking booking;
    private MovieApiService apiService;

    // Polling & Timer variables
    private Handler pollingHandler = new Handler();
    private Runnable pollingRunnable;
    private String currentBookingId;
    private CountDownTimer countDownTimer;

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
            tvBookingId.setText("Đơn hàng mới nhất");
            tvMovieTitle.setText("Đang tải...");
            tvTotal.setText("0đ");
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
        tvExpiry = findViewById(R.id.tv_payment_expiry);
        tvBankInfo = findViewById(R.id.tv_payment_bank_info);
        ivQrCode = findViewById(R.id.iv_qr_code);
        cvQrContainer = findViewById(R.id.cv_qr_container);
        btnConfirm = findViewById(R.id.btn_confirm_payment);
        pbLoading = findViewById(R.id.pb_payment_loading);

        btnConfirm.setOnClickListener(v -> createVietQR());
    }

    private void displayInfo() {
        String code = booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
        tvBookingId.setText(code);
        if (booking.getShowtime() != null && booking.getShowtime().getMovie() != null) {
            tvMovieTitle.setText(booking.getShowtime().getMovie().getTitle());
        }
        tvTotal.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
    }

    private void createVietQR() {
        setLoading(true);
        Call<ApiResponse<PaymentResponse>> call;

        if (booking != null) {
            String bookingIdToSend = booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
            call = apiService.createVietQR(new PaymentRequest(bookingIdToSend));
        } else {
            call = apiService.createVietQRAuto();
        }

        call.enqueue(new Callback<ApiResponse<PaymentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentResponse>> call, Response<ApiResponse<PaymentResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    PaymentResponse result = response.body().getResult();
                    updateUIWithResult(result);
                    
                    if (result.getBookingId() != null) {
                        startPaymentPolling(result.getBookingId());
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Không tìm thấy đơn hàng cần thanh toán", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPaymentPolling(String bookingId) {
        stopPolling();
        this.currentBookingId = bookingId;
        
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkPaymentStatus();
            }
        };
        pollingHandler.postDelayed(pollingRunnable, 3000);
    }

    private void checkPaymentStatus() {
        if (currentBookingId == null) return;

        apiService.checkPaymentStatus(currentBookingId).enqueue(new Callback<ApiResponse<PaymentStatusResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentStatusResponse>> call, Response<ApiResponse<PaymentStatusResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    PaymentStatusResponse statusData = response.body().getResult();
                    String status = statusData.getStatus();

                    if ("PAID".equalsIgnoreCase(status)) {
                        navigateToResult(true);
                    } else if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
                        navigateToResult(false);
                    } else {
                        pollingHandler.postDelayed(pollingRunnable, 3000);
                    }
                } else {
                    pollingHandler.postDelayed(pollingRunnable, 3000);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentStatusResponse>> call, Throwable t) {
                pollingHandler.postDelayed(pollingRunnable, 3000);
            }
        });
    }

    private void navigateToResult(boolean isSuccess) {
        stopPolling();
        if (countDownTimer != null) countDownTimer.cancel();
        
        Intent intent = new Intent(PaymentActivity.this, PaymentResultActivity.class);
        intent.putExtra("IS_SUCCESS", isSuccess);
        intent.putExtra("BOOKING_ID", currentBookingId);
        startActivity(intent);
        finish();
    }

    private void stopPolling() {
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    private void updateUIWithResult(PaymentResponse result) {
        if (result.getBookingId() != null) {
            tvBookingId.setText(result.getBookingId());
        }
        if (result.getTotalPrice() != null) {
            tvTotal.setText(CurrencyUtils.formatPrice(result.getTotalPrice()));
        }

        if (result.getQrData() != null) {
            cvQrContainer.setVisibility(View.VISIBLE);
            Glide.with(this).load(result.getQrData().getQrDataURL()).into(ivQrCode);

            String bankInfo = String.format("%s\nSTK: %s\nChủ TK: %s", 
                    result.getQrData().getBankName(),
                    result.getQrData().getBankAccount(),
                    result.getQrData().getUserBankName());
            tvBankInfo.setText(bankInfo);

            // Bắt đầu đếm ngược thời gian hết hạn
            int minutes = result.getRemainingMinutes() != null ? result.getRemainingMinutes() : 5;
            startTimer(minutes);

            btnConfirm.setText("Đang chờ thanh toán...");
            btnConfirm.setEnabled(false);
        }
    }

    private void startTimer(int minutes) {
        if (countDownTimer != null) countDownTimer.cancel();
        
        countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long m = (millisUntilFinished / 1000) / 60;
                long s = (millisUntilFinished / 1000) % 60;
                tvExpiry.setText(String.format("Mã sẽ hết hạn sau: %02d:%02d", m, s));
            }

            @Override
            public void onFinish() {
                tvExpiry.setText("Mã đã hết hạn");
                navigateToResult(false); // Chuyển sang fail khi hết giờ
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirm.setEnabled(!isLoading);
        btnConfirm.setText(isLoading ? "Đang xử lý..." : "Tạo mã VietQR");
    }
}
