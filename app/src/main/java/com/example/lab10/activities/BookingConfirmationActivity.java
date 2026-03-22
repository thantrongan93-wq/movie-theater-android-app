package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.MainActivity;
import com.example.lab10.R;
import com.example.lab10.models.Booking;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;

public class BookingConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "extra_booking";

    private static final long PAYMENT_WINDOW_MS = 5 * 60 * 1000L; // 5 phút

    private TextView tvBookingCode, tvMovieTitle, tvShowDateTime,
            tvTheaterName, tvSeats, tvTotalPrice, tvStatus, tvFoodSummary;
    private TextView tvPaymentCountdown;
    private Button   btnProceedPayment, btnBackToHome;

    private Booking booking;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        booking = (Booking) getIntent().getSerializableExtra(EXTRA_BOOKING);
        if (booking == null) {
            finish();
            return;
        }

        initViews();
        displayBookingDetails();
        startPaymentCountdown();
    }

    private void initViews() {
        tvBookingCode       = findViewById(R.id.tv_booking_code);
        tvMovieTitle        = findViewById(R.id.tv_movie_title);
        tvShowDateTime      = findViewById(R.id.tv_show_datetime);
        tvTheaterName       = findViewById(R.id.tv_theater_name);
        tvSeats             = findViewById(R.id.tv_seats);
        tvTotalPrice        = findViewById(R.id.tv_total_price);
        tvStatus            = findViewById(R.id.tv_status);
        tvFoodSummary       = findViewById(R.id.tv_food_summary);
        tvPaymentCountdown  = findViewById(R.id.tv_payment_countdown);
        btnProceedPayment   = findViewById(R.id.btn_proceed_payment);
        btnBackToHome       = findViewById(R.id.btn_back_to_home);

        btnProceedPayment.setOnClickListener(v -> goToPayment());

        btnBackToHome.setOnClickListener(v -> {
            cancelCountdown();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayBookingDetails() {
        // Mã đặt vé
        String code = booking.getBookingUuid() != null
                ? booking.getBookingUuid()
                : (booking.getId() != null ? "#" + booking.getId() : "N/A");
        tvBookingCode.setText("Mã đặt vé: " + code);

        // Lấy từ Intent (confirm response không trả về movie/seat info)
        String movieTitle = getIntent().getStringExtra("MOVIE_TITLE");
        String seatsInfo  = getIntent().getStringExtra("SEATS_INFO");
        Showtime showtime = (Showtime) getIntent().getSerializableExtra(
                SeatSelectionActivity.EXTRA_SHOWTIME);

        tvMovieTitle.setText(movieTitle != null ? movieTitle : "N/A");
        tvSeats.setText("Ghế: " + (seatsInfo != null ? seatsInfo : "N/A"));

        if (showtime != null) {
            tvShowDateTime.setText(showtime.getStartTime() != null
                    ? showtime.getStartTime() : "N/A");
            tvTheaterName.setText(showtime.getCinemaRoomName() != null
                    ? showtime.getCinemaRoomName()
                    : (showtime.getRoomId() != null ? "Phòng " + showtime.getRoomId() : "N/A"));
        } else {
            tvShowDateTime.setText("N/A");
            tvTheaterName.setText("N/A");
        }

        tvTotalPrice.setText(CurrencyUtils.formatPrice(
                booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0));
        tvStatus.setText("Trạng thái: " + (booking.getStatus() != null
                ? booking.getStatus() : "CONFIRMED"));

        // Tóm tắt đồ ăn
        String foodSummary = getIntent().getStringExtra("FOOD_SUMMARY");
        if (tvFoodSummary != null) {
            if (foodSummary != null && !foodSummary.isEmpty()) {
                tvFoodSummary.setText("Đồ ăn:\n" + foodSummary);
                tvFoodSummary.setVisibility(View.VISIBLE);
            } else {
                tvFoodSummary.setVisibility(View.GONE);
            }
        }
    }

    /** Đếm ngược 5 phút để user hoàn tất thanh toán */
    private void startPaymentCountdown() {
        countDownTimer = new CountDownTimer(PAYMENT_WINDOW_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = millisUntilFinished / 60000;
                long sec = (millisUntilFinished % 60000) / 1000;
                String msg = String.format("Vui lòng thanh toán trong: %02d:%02d", min, sec);
                if (tvPaymentCountdown != null) tvPaymentCountdown.setText(msg);
            }

            @Override
            public void onFinish() {
                if (tvPaymentCountdown != null) {
                    tvPaymentCountdown.setText("⚠ Hết thời gian thanh toán");
                    tvPaymentCountdown.setTextColor(0xFFD32F2F);
                }
                if (btnProceedPayment != null) {
                    btnProceedPayment.setEnabled(false);
                    btnProceedPayment.setText("Hết thời gian thanh toán");
                }
            }
        }.start();
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void goToPayment() {
        cancelCountdown();
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_BOOKING, booking);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        cancelCountdown();
        super.onDestroy();
    }
}