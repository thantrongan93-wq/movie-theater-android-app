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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.SeatAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.BookingRequest;
import com.example.lab10.models.Movie;
import com.example.lab10.models.Seat;
import com.example.lab10.models.SeatResponse;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;
import com.example.lab10.utils.NotificationHelper;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_MOVIE    = "extra_movie";

    private static final String TAG = "SeatSelection";

    // Views
    private ImageView   ivBack;
    private TextView    tvMovieTitle, tvShowDateTime, tvTheaterName;
    private TextView    tvSelectedSeats, tvTotalPrice;
    private TextView    tvSeatsEmpty;
    private RecyclerView rvSeats;
    private Button      btnBookNow;
    private ProgressBar progressBar;

    // Data
    private Showtime        showtime;
    private Movie           movie;
    private SeatAdapter     seatAdapter;
    private MovieApiService apiService;
    private SessionManager  sessionManager;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        sessionManager = new SessionManager(this);

        // Restore JWT token if process was restarted
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();

        showtime = (Showtime) getIntent().getSerializableExtra(EXTRA_SHOWTIME);
        movie    = (Movie)    getIntent().getSerializableExtra(EXTRA_MOVIE);

        if (showtime == null || movie == null) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayShowtimeInfo();
        loadSeats();
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    private void initViews() {
        ivBack           = findViewById(R.id.iv_back);
        tvMovieTitle     = findViewById(R.id.tv_movie_title);
        tvShowDateTime   = findViewById(R.id.tv_show_datetime);
        tvTheaterName    = findViewById(R.id.tv_theater_name);
        tvSelectedSeats  = findViewById(R.id.tv_selected_seats);
        tvTotalPrice     = findViewById(R.id.tv_total_price);
        tvSeatsEmpty     = findViewById(R.id.tv_seats_empty);
        rvSeats          = findViewById(R.id.rv_seats);
        btnBookNow       = findViewById(R.id.btn_book_now);
        progressBar      = findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(v -> finish());
        btnBookNow.setOnClickListener(v -> bookSeats());

        // Mỗi item là một hàng ghế (A, B, C...)
        rvSeats.setLayoutManager(new LinearLayoutManager(this));
        seatAdapter = new SeatAdapter(new ArrayList<>(), this::onSeatSelectionChanged);
        rvSeats.setAdapter(seatAdapter);
    }

    // -------------------------------------------------------------------------
    // Display showtime info at top of screen
    // -------------------------------------------------------------------------

    private void displayShowtimeInfo() {
        tvMovieTitle.setText(movie.getTitle() != null ? movie.getTitle() : "");

        // Date
        String date = showtime.getShowDate() != null
                ? DateTimeUtils.formatDate(showtime.getShowDate()) : "";

        // Time range
        String startT = DateTimeUtils.formatTime(showtime.getStartTime());
        String endT   = DateTimeUtils.formatTime(showtime.getEndTime());
        String timeStr = startT + (!endT.isEmpty() ? " - " + endT : "");

        tvShowDateTime.setText((date + "  " + timeStr).trim());

        // Theater / room name
        if (showtime.getTheater() != null && showtime.getTheater().getName() != null) {
            tvTheaterName.setText(showtime.getTheater().getName());
        } else if (showtime.getRoom() != null && showtime.getRoom().getName() != null) {
            tvTheaterName.setText("Phòng: " + showtime.getRoom().getName());
        } else if (showtime.getCinemaRoomName() != null) {
            tvTheaterName.setText(showtime.getCinemaRoomName());
        } else if (showtime.getRoomId() != null) {
            tvTheaterName.setText("Phòng: " + showtime.getRoomId());
        } else {
            tvTheaterName.setText("");
        }
    }

    // -------------------------------------------------------------------------
    // Load seats — primary: showtime-details/{id}/seats, fallback: seats/room/{id}
    // -------------------------------------------------------------------------

    private void loadSeats() {
        progressBar.setVisibility(View.VISIBLE);
        rvSeats.setVisibility(View.GONE);
        tvSeatsEmpty.setVisibility(View.GONE);
        btnBookNow.setEnabled(false);

        Long showtimeDetailId = showtime.getShowtimeDetailId();
        Long roomId           = showtime.getRoomId();

        Log.d(TAG, "loadSeats — showtimeDetailId=" + showtimeDetailId
                + "  roomId=" + roomId + "  id=" + showtime.getId());

        // Primary: GET /api/showtime-details/{id}/seats (correct availability status)
        if (showtimeDetailId != null) {
            apiService.getSeatsForShowtimeDetail(showtimeDetailId)
                    .enqueue(new Callback<ApiResponse<SeatResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<SeatResponse>> call,
                                               Response<ApiResponse<SeatResponse>> response) {
                            Log.d(TAG, "showtime-details seats HTTP " + response.code());
                            if (response.isSuccessful() && response.body() != null) {
                                SeatResponse seatResponse = response.body().getResult();
                                if (seatResponse != null && seatResponse.getSeats() != null
                                        && !seatResponse.getSeats().isEmpty()) {
                                    onSeatsLoaded(seatResponse.getSeats());
                                    return;
                                }
                            }
                            onSeatsEmpty("Không tải được danh sách ghế");
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<SeatResponse>> call, Throwable t) {
                            Log.e(TAG, "showtime-details seats failed: " + t.getMessage());
                            onSeatsEmpty("Lỗi kết nối: " + t.getMessage());
                        }
                    });
            return;
        }

        // No showtimeDetailId → go straight to room fallback
        loadSeatsByRoom(roomId);
    }

    private void loadSeatsByRoom(Long roomId) {
        if (roomId == null) {
            onSeatsEmpty("Không tìm thấy thông tin phòng chiếu");
            return;
        }

        Log.d(TAG, "loadSeatsByRoom roomId=" + roomId);

        apiService.getSeatsByRoom(roomId).enqueue(new Callback<ApiResponse<List<Seat>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Seat>>> call,
                                   Response<ApiResponse<List<Seat>>> response) {
                Log.d(TAG, "seats/room HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<Seat> seats = response.body().getResult();
                    if (seats != null && !seats.isEmpty()) {
                        onSeatsLoaded(seats);
                        return;
                    }
                }
                onSeatsEmpty("Phòng chiếu chưa có sơ đồ ghế");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Seat>>> call, Throwable t) {
                Log.e(TAG, "seats/room failed: " + t.getMessage());
                onSeatsEmpty("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void onSeatsLoaded(List<Seat> seats) {
        progressBar.setVisibility(View.GONE);
        tvSeatsEmpty.setVisibility(View.GONE);
        rvSeats.setVisibility(View.VISIBLE);
        seatAdapter.updateSeats(seats);
        Log.d(TAG, "Loaded " + seats.size() + " seats");
    }

    private void onSeatsEmpty(String message) {
        progressBar.setVisibility(View.GONE);
        rvSeats.setVisibility(View.GONE);
        tvSeatsEmpty.setVisibility(View.VISIBLE);
        tvSeatsEmpty.setText(message);
        btnBookNow.setEnabled(false);
        Log.d(TAG, "Seats empty: " + message);
    }

    // -------------------------------------------------------------------------
    // Seat selection changed → update summary bar
    // -------------------------------------------------------------------------

    private void onSeatSelectionChanged() {
        List<Seat> selectedSeats = seatAdapter.getSelectedSeats();

        if (selectedSeats.isEmpty()) {
            tvSelectedSeats.setText("Chưa chọn ghế");
            tvTotalPrice.setText("0 VNĐ");
            btnBookNow.setEnabled(false);
        } else {
            // Build label "A1, A2, B3"
            String seatLabels = selectedSeats.stream()
                    .map(s -> {
                        String row = s.getRowNumber()  != null ? s.getRowNumber()  : "";
                        String num = s.getSeatNumber() != null ? s.getSeatNumber() : "";
                        return row + num;
                    })
                    .collect(Collectors.joining(", "));
            tvSelectedSeats.setText("Ghế: " + seatLabels);

            double totalPrice = selectedSeats.stream()
                    .mapToDouble(s -> {
                        if (s.getPrice() != null) return s.getPrice();
                        return showtime.getPrice() != null ? showtime.getPrice() : 0.0;
                    })
                    .sum();
            tvTotalPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            btnBookNow.setEnabled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Book seats — POST /api/booking/create
    // -------------------------------------------------------------------------

    private void bookSeats() {
        List<Seat> selectedSeats = seatAdapter.getSelectedSeats();
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt vé", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnBookNow.setEnabled(false);

        List<Long> seatIds = selectedSeats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());

        // showtime.getId() returns showtimeDetailId if available, else showtimeId
        BookingRequest request = new BookingRequest(showtime.getId(), seatIds);
        Log.d(TAG, "createBooking — showtimeDetailId=" + showtime.getId()
                + "  seatIds=" + seatIds);

        apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                Log.d(TAG, "createBooking HTTP " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        progressBar.setVisibility(View.GONE);

                        try {
                            NotificationHelper.sendBookingConfirmationNotification(
                                    SeatSelectionActivity.this, booking);
                        } catch (Exception e) {
                            Log.e(TAG, "send notification failed", e);
                        }

                        Toast.makeText(SeatSelectionActivity.this,
                                "Đặt vé thành công! Chờ xác nhận.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SeatSelectionActivity.this,
                                FoodOrderActivity.class);
                        intent.putExtra("BOOKING_ID", resolveBookingId(booking));
                        intent.putExtra("MOVIE_TITLE", movie.getTitle());
                        intent.putExtra("SEATS_INFO", getSelectedSeatsLabel());
                        intent.putExtra("SEAT_PRICE", booking.getTotalPrice() != null
                                ? booking.getTotalPrice() : 0.0);
                        intent.putExtra("REMAINING_MINUTES",
                                booking.getRemainingMinutes() != null
                                        ? booking.getRemainingMinutes() : 2);
                        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnBookNow.setEnabled(true);
                        String msg = apiResponse.getMessage() != null
                                ? apiResponse.getMessage() : "Đặt vé thất bại";
                        Toast.makeText(SeatSelectionActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } else if (response.code() == 409) {
                    // Có booking PENDING cũ → cancel rồi thử lại
                    Log.d(TAG, "409 unfinished booking → auto cancel and retry");
                    cancelAndRetry(request);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnBookNow.setEnabled(true);
                    try {
                        String err = response.errorBody() != null
                                ? response.errorBody().string() : "";
                        Log.e(TAG, "HTTP error " + response.code() + ": " + err);
                    } catch (Exception ignored) {}
                    Toast.makeText(SeatSelectionActivity.this,
                            "Đặt vé thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);
                Log.e(TAG, "createBooking failed: " + t.getMessage(), t);
                Toast.makeText(SeatSelectionActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void cancelAndRetry(BookingRequest originalRequest) {
        apiService.cancelPendingBooking().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                Log.d(TAG, "cancelPendingBooking HTTP " + response.code());
                if (response.isSuccessful()) {
                    // Cancel xong → thử đặt lại
                    Log.d(TAG, "Cancelled old booking, retrying...");
                    apiService.createBooking(originalRequest)
                            .enqueue(new Callback<ApiResponse<Booking>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Booking>> call,
                                                       Response<ApiResponse<Booking>> response) {
                                    if (response.isSuccessful() && response.body() != null
                                            && response.body().getResult() != null) {
                                        Booking booking = response.body().getResult();
                                        progressBar.setVisibility(View.GONE);

                                        try {
                                            NotificationHelper.sendBookingConfirmationNotification(
                                                    SeatSelectionActivity.this, booking);
                                        } catch (Exception e) {
                                            Log.e(TAG, "send notification failed", e);
                                        }

                                        Toast.makeText(SeatSelectionActivity.this,
                                                "Đặt vé thành công! Chờ xác nhận.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(SeatSelectionActivity.this,
                                                FoodOrderActivity.class);
                                        intent.putExtra("BOOKING_ID", resolveBookingId(booking));
                                        intent.putExtra("MOVIE_TITLE", movie.getTitle());
                                        intent.putExtra("SEATS_INFO", getSelectedSeatsLabel());
                                        intent.putExtra("SEAT_PRICE", booking.getTotalPrice() != null
                                                ? booking.getTotalPrice() : 0.0);
                                        intent.putExtra("REMAINING_MINUTES",
                                                booking.getRemainingMinutes() != null
                                                        ? booking.getRemainingMinutes() : 2);
                                        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        btnBookNow.setEnabled(true);
                                        Toast.makeText(SeatSelectionActivity.this,
                                                "Đặt vé thất bại sau khi thử lại",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    btnBookNow.setEnabled(true);
                                    Toast.makeText(SeatSelectionActivity.this,
                                            "Lỗi kết nối khi thử lại: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnBookNow.setEnabled(true);
                    Toast.makeText(SeatSelectionActivity.this,
                            "Không thể hủy booking cũ, vui lòng thử lại sau",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);
                Log.e(TAG, "cancelPendingBooking failed: " + t.getMessage());
                Toast.makeText(SeatSelectionActivity.this,
                        "Lỗi kết nối khi hủy booking cũ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getSelectedSeatsLabel() {
        return seatAdapter.getSelectedSeats().stream()
                .map(s -> s.getSeatNumber() != null ? s.getSeatNumber() : "")
                .collect(Collectors.joining(", "));
    }

    private String resolveBookingId(Booking booking) {
        if (booking == null) return null;
        if (booking.getBookingUuid() != null && !booking.getBookingUuid().isEmpty()) {
            return booking.getBookingUuid();
        }
        if (booking.getBookingCode() != null && !booking.getBookingCode().isEmpty()) {
            return booking.getBookingCode();
        }
        return booking.getId() != null ? String.valueOf(booking.getId()) : null;
    }
}