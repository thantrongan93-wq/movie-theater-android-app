package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.utils.NotificationHelper;
import com.example.lab10.R;
import com.example.lab10.adapters.SeatAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.Movie;
import com.example.lab10.models.Seat;
import com.example.lab10.models.Showtime;
import com.example.lab10.models.BookingRequest;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatSelectionActivity extends AppCompatActivity {
    
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_MOVIE = "extra_movie";
    
    private ImageView ivBack;
    private TextView tvMovieTitle, tvShowDateTime, tvTheaterName, tvSelectedSeats, tvTotalPrice;
    private RecyclerView rvSeats;
    private Button btnBookNow;
    private ProgressBar progressBar;
    
    private Showtime showtime;
    private Movie movie;
    private SeatAdapter seatAdapter;
    private MovieApiService apiService;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        
        sessionManager = new SessionManager(this);

        // Khôi phục JWT token nếu bị mất (static field bị clear khi process restart)
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();

        showtime = (Showtime) getIntent().getSerializableExtra(EXTRA_SHOWTIME);
        movie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        
        if (showtime == null || movie == null) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        displayShowtimeInfo();
        loadSeats();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvShowDateTime = findViewById(R.id.tv_show_datetime);
        tvTheaterName = findViewById(R.id.tv_theater_name);
        tvSelectedSeats = findViewById(R.id.tv_selected_seats);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        rvSeats = findViewById(R.id.rv_seats);
        btnBookNow = findViewById(R.id.btn_book_now);
        progressBar = findViewById(R.id.progress_bar);
        
        ivBack.setOnClickListener(v -> finish());
        btnBookNow.setOnClickListener(v -> bookSeats());
        
        rvSeats.setLayoutManager(new GridLayoutManager(this, 6));
        seatAdapter = new SeatAdapter(new ArrayList<>(), this::onSeatSelectionChanged);
        rvSeats.setAdapter(seatAdapter);
    }
    
    private void displayShowtimeInfo() {
        tvMovieTitle.setText(movie.getTitle());
        String date = showtime.getShowDate() != null ? DateTimeUtils.formatDate(showtime.getShowDate()) : "";
        String startT = DateTimeUtils.formatTime(showtime.getStartTime());
        String endT   = DateTimeUtils.formatTime(showtime.getEndTime());
        String timeStr = startT + (!endT.isEmpty() ? " - " + endT : "");
        tvShowDateTime.setText((date + " " + timeStr).trim());
        if (showtime.getTheater() != null) {
            tvTheaterName.setText(showtime.getTheater().getName());
        } else if (showtime.getRoom() != null) {
            tvTheaterName.setText("Phòng: " + showtime.getRoom().getName());
        } else if (showtime.getCinemaRoomName() != null) {
            tvTheaterName.setText(showtime.getCinemaRoomName());
        } else if (showtime.getRoomId() != null) {
            tvTheaterName.setText("Phòng: " + showtime.getRoomId());
        }
    }
    
    private void loadSeats() {
        progressBar.setVisibility(View.VISIBLE);

        Long showtimeDetailId = showtime.getShowtimeDetailId();
        Long roomId = showtime.getRoomId();
        Log.d("SEATS", "showtimeDetailId=" + showtimeDetailId + " roomId=" + roomId + " showtimeId=" + showtime.getId());

        // Ưu tiên getSeatsForShowtimeDetail vì trả về đúng trạng thái ghế (available/booked)
        if (showtimeDetailId != null) {
            apiService.getSeatsForShowtimeDetail(showtimeDetailId).enqueue(new Callback<ApiResponse<List<Seat>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Seat>>> call, Response<ApiResponse<List<Seat>>> response) {
                    progressBar.setVisibility(View.GONE);
                    Log.d("SEATS", "detail HTTP " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<Seat> seats = response.body().getResult();
                        Log.d("SEATS", "detail seats size=" + (seats != null ? seats.size() : 0));
                        if (seats != null && !seats.isEmpty()) {
                            seatAdapter.updateSeats(seats);
                            return;
                        }
                    }
                    // fallback theo phòng
                    loadSeatsByRoom(roomId);
                }
                @Override
                public void onFailure(Call<ApiResponse<List<Seat>>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("SEATS", "detail onFailure: " + t.getMessage());
                    loadSeatsByRoom(roomId);
                }
            });
            return;
        }

        loadSeatsByRoom(roomId);
    }

    private void loadSeatsByRoom(Long roomId) {
        if (roomId == null) {
            Toast.makeText(this, "Không tìm thấy phòng chiếu", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        Log.d("SEATS", "loadSeatsByRoom roomId=" + roomId);
        apiService.getSeatsByRoom(roomId).enqueue(new Callback<ApiResponse<List<Seat>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Seat>>> call, Response<ApiResponse<List<Seat>>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("SEATS", "HTTP " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Seat>> apiResponse = response.body();
                    Log.d("SEATS", "api code=" + apiResponse.getCode() + " msg=" + apiResponse.getMessage());
                    List<Seat> seats = apiResponse.getResult();
                    Log.d("SEATS", "seats null=" + (seats == null) + " size=" + (seats != null ? seats.size() : 0));
                    if (seats != null && !seats.isEmpty()) {
                        seatAdapter.updateSeats(seats);
                    } else {
                        try {
                            // Doc raw body neu result null
                            String raw = response.errorBody() != null ? response.errorBody().string() : "";
                            Log.e("SEATS", "errorBody=" + raw);
                        } catch (Exception ignored) {}
                        Toast.makeText(SeatSelectionActivity.this, "Không có ghế trong phòng này", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "";
                        Log.e("SEATS", "Error " + response.code() + ": " + err);
                        Toast.makeText(SeatSelectionActivity.this, "Lỗi " + response.code() + ": " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(SeatSelectionActivity.this, "Không tải được danh sách ghế", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Seat>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("SEATS", "onFailure: " + t.getMessage(), t);
                Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void onSeatSelectionChanged() {
        List<Seat> selectedSeats = seatAdapter.getSelectedSeats();
        
        if (selectedSeats.isEmpty()) {
            tvSelectedSeats.setText("Chưa chọn ghế");
            tvTotalPrice.setText("0 VNĐ");
            btnBookNow.setEnabled(false);
        } else {
            String seatNumbers = selectedSeats.stream()
                    .map(seat -> seat.getSeatNumber() != null ? seat.getSeatNumber()
                            : (seat.getRowNumber() != null ? seat.getRowNumber() : ""))
                    .collect(Collectors.joining(", "));
            tvSelectedSeats.setText("Ghế: " + seatNumbers);

            double unitPrice = showtime.getPrice() != null ? showtime.getPrice() : 0.0;
            double totalPrice = selectedSeats.size() * unitPrice;
            tvTotalPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            btnBookNow.setEnabled(true);
        }
    }
    
    private void bookSeats() {
        List<Seat> selectedSeats = seatAdapter.getSelectedSeats();
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt vé", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnBookNow.setEnabled(false);

        List<Long> seatIds = selectedSeats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());

        double totalPrice = selectedSeats.size() * (showtime.getPrice() != null ? showtime.getPrice() : 0);

        // Dùng BookingRequest mới: chỉ cần showtimeDetailId + seatIds
        // Server tự lấy userId từ JWT
        BookingRequest bookingRequest = new BookingRequest(
                showtime.getId(),  // showtimeDetailId
                seatIds
        );
        Log.d("BOOKING", "showtimeDetailId=" + showtime.getId() + " seatIds=" + seatIds);

        apiService.createBooking(bookingRequest).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        NotificationHelper.sendBookingConfirmationNotification(
                                SeatSelectionActivity.this, booking);
                        Toast.makeText(SeatSelectionActivity.this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SeatSelectionActivity.this, BookingConfirmationActivity.class);
                        intent.putExtra(BookingConfirmationActivity.EXTRA_BOOKING, booking); // ← CHANGED
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đặt vé thất bại";
                        Toast.makeText(SeatSelectionActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Đặt vé thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);
                Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
