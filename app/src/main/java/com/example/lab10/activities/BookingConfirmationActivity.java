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
    
    private TextView tvBookingCode, tvMovieTitle, tvShowDateTime, tvTheaterName, tvSeats, tvTotalPrice, tvStatus;
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
        tvBookingCode = findViewById(R.id.tv_booking_code);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvShowDateTime = findViewById(R.id.tv_show_datetime);
        tvTheaterName = findViewById(R.id.tv_theater_name);
        tvSeats = findViewById(R.id.tv_seats);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvStatus = findViewById(R.id.tv_status);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
        btnViewBookings = findViewById(R.id.btn_view_bookings);
        
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void displayBookingDetails() {
        tvBookingCode.setText("Mã đặt vé: " + booking.getBookingCode());
        
        if (booking.getShowtime() != null && booking.getShowtime().getMovie() != null) {
            tvMovieTitle.setText(booking.getShowtime().getMovie().getTitle());
            tvShowDateTime.setText(DateTimeUtils.formatDate(booking.getShowtime().getShowDate()) + " " +
                                  DateTimeUtils.formatTime(booking.getShowtime().getShowTime()));
            
            if (booking.getShowtime().getTheater() != null) {
                tvTheaterName.setText(booking.getShowtime().getTheater().getName());
            }
        }
        
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            String seatNumbers = booking.getSeats().stream()
                    .map(seat -> seat.getRowNumber() + seat.getSeatNumber())
                    .collect(Collectors.joining(", "));
            tvSeats.setText("Ghế: " + seatNumbers);
        }
        
        tvTotalPrice.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
        tvStatus.setText("Trạng thái: " + booking.getStatus());
    }
}
