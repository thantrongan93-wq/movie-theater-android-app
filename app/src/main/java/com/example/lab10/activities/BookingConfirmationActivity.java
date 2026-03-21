package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.MainActivity;
import com.example.lab10.R;
import com.example.lab10.models.Booking;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;

import java.util.stream.Collectors;

public class BookingConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "extra_booking";

    private TextView tvBookingCode, tvMovieTitle, tvShowDateTime,
            tvTheaterName, tvSeats, tvTotalPrice, tvStatus, tvFoodSummary;
    private Button btnBackToHome, btnViewBookings;
    private Booking booking;

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
    }

    private void initViews() {
        tvBookingCode  = findViewById(R.id.tv_booking_code);
        tvMovieTitle   = findViewById(R.id.tv_movie_title);
        tvShowDateTime = findViewById(R.id.tv_show_datetime);
        tvTheaterName  = findViewById(R.id.tv_theater_name);
        tvSeats        = findViewById(R.id.tv_seats);
        tvTotalPrice   = findViewById(R.id.tv_total_price);
        tvStatus       = findViewById(R.id.tv_status);
        tvFoodSummary  = findViewById(R.id.tv_food_summary);
        btnBackToHome  = findViewById(R.id.btn_back_to_home);
        btnViewBookings = findViewById(R.id.btn_view_bookings);

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnViewBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
            finish();
        });
    }

    private void displayBookingDetails() {
        String code = booking.getBookingUuid() != null
                ? booking.getBookingUuid()
                : (booking.getId() != null ? "#" + booking.getId() : "N/A");
        tvBookingCode.setText("Mã đặt vé: " + code);

        String movieTitle = getIntent().getStringExtra("MOVIE_TITLE");
        String seatsInfo  = getIntent().getStringExtra("SEATS_INFO");
        com.example.lab10.models.Showtime showtime =
                (com.example.lab10.models.Showtime) getIntent().getSerializableExtra(
                        SeatSelectionActivity.EXTRA_SHOWTIME);

        tvMovieTitle.setText(movieTitle != null ? movieTitle : "N/A");
        tvSeats.setText("Ghế: " + (seatsInfo != null ? seatsInfo : "N/A"));

        if (showtime != null) {
            tvShowDateTime.setText(showtime.getStartTime() != null ? showtime.getStartTime() : "N/A");
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

        String foodSummary = getIntent().getStringExtra("FOOD_SUMMARY");
        if (tvFoodSummary != null) {
            if (foodSummary != null && !foodSummary.isEmpty()) {
                tvFoodSummary.setText("Đồ ăn:\n" + foodSummary);
                tvFoodSummary.setVisibility(android.view.View.VISIBLE);
            } else {
                tvFoodSummary.setVisibility(android.view.View.GONE);
            }
        }
    }
}