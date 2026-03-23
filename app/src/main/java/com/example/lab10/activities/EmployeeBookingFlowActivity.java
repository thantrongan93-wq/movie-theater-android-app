package com.example.lab10.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.lab10.R;
import com.example.lab10.adapters.BookingShowtimeAdapter;
import com.example.lab10.adapters.FoodAdapter;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.adapters.SeatAdapter;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.api.ApiClient;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.BookingRequest;
import com.example.lab10.models.FoodCombo;
import com.example.lab10.models.FoodItem;
import com.example.lab10.models.FoodOrderRequest;
import com.example.lab10.models.PaymentRequest;
import com.example.lab10.models.PaymentResponse;
import com.example.lab10.models.PaymentStatusResponse;
import com.example.lab10.models.Seat;
import com.example.lab10.models.SeatResponse;
import com.example.lab10.models.ShowtimeGroup;
import com.example.lab10.models.LoyaltyResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;
import com.example.lab10.utils.SessionManager;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeBookingFlowActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeBookingFlow";
    private static final int TOTAL_STEPS = 6;
    private static final int[] UPCOMING_PAGE_SIZES = {50, 20, 10};

    // Flow state
    private int currentStep = 0;
    private Long selectedMovieId = null;
    private Long selectedShowtimeId = null;
    private Showtime selectedShowtime = null;
    private List<Seat> selectedSeats = new ArrayList<>();
    private String currentBookingId = null;
    private Booking currentBooking = null;
    
    // UI - Main
    private TextView[] stepIndicators;
    private View[] stepContainers; // Changed from LinearLayout[] to View[] for ScrollView support
    private Button btnPrevious, btnNext, btnRestartFlow;
    
    // Step 0: Movies
    private RecyclerView rvMovies;
    private ProgressBar progressMovies;
    private TextView tvMoviesEmpty;
    private MovieAdapter movieAdapter;
    
    // Step 1: Showtimes
    private RecyclerView rvShowtimes;
    private ProgressBar progressShowtimes;
    private TextView tvShowtimesEmpty;
    private BookingShowtimeAdapter showtimeAdapter;
    
    // Step 2: Seats
    private RecyclerView rvSeats;
    private ProgressBar progressSeats;
    private TextView tvSeatsEmpty;
    private TextView tvSelectedSeatsInfo;
    private TextView tvSeatTotalPrice;
    private SeatAdapter seatAdapter;
    
    // Step 3: Food
    private RecyclerView rvFoodItems;
    private RecyclerView rvFoodCombos;
    private EditText etPhone;
    private EditText etPromotionId;
    private EditText etCouponCode;
    private EditText etPoints;
    private TextView tvUserPoints;
    private TextView tvCountdown;
    private TextView tvFoodTotal;
    private ProgressBar progressFood;
    private List<FoodItem> foodItems = new ArrayList<>();
    private List<FoodCombo> foodCombos = new ArrayList<>();
    private FoodAdapter<FoodItem> foodItemAdapter;
    private FoodAdapter<FoodCombo> foodComboAdapter;
    
    // Step 4: Payment
    private TextView tvPaymentBookingInfo;
    private Button btnPayCash;
    private Button btnPayBank;
    private EditText etCashAmount;
    private ProgressBar progressPayment;
    private ImageView ivQrCode;
    private View cvQrContainer;
    private TextView tvBankInfo;
    private TextView tvPaymentCountdown;
    
    // Step 5: Completion
    private TextView tvCompletionSummary;
    private Button btnNewBooking;
    private Button btnClose;
    
    // API
    private MovieApiService apiService;
    private SessionManager sessionManager;
    
    // Timers
    private CountDownTimer countDownTimer;
    private Handler pollingHandler = new Handler();
    private Runnable pollingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_booking_flow);

        sessionManager = new SessionManager(this);
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();
        setupToolbar();
        initViews();
        setupActions();
        renderStep();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopPolling();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_booking_flow);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Employee Booking Flow");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        stepIndicators = new TextView[] {
                findViewById(R.id.step_movie),
                findViewById(R.id.step_time),
                findViewById(R.id.step_seat),
                findViewById(R.id.step_confirm),
                findViewById(R.id.step_done)
        };

        stepContainers = new View[] {
                findViewById(R.id.layout_step_movie),
                findViewById(R.id.layout_step_time),
                findViewById(R.id.layout_step_seat),
                findViewById(R.id.layout_step_confirm),
                findViewById(R.id.layout_step_payment),
                findViewById(R.id.layout_step_done)
        };

        btnPrevious = findViewById(R.id.btn_previous_step);
        btnNext = findViewById(R.id.btn_next_step);
        btnRestartFlow = findViewById(R.id.btn_restart_flow);
        
        // Step 0
        rvMovies = findViewById(R.id.rv_movies);
        progressMovies = findViewById(R.id.progress_movies);
        tvMoviesEmpty = findViewById(R.id.tv_movies_empty);
        
        if (rvMovies != null) {
            rvMovies.setLayoutManager(new LinearLayoutManager(this));
            movieAdapter = new MovieAdapter(new ArrayList<>(), this::onMovieClick);
            rvMovies.setAdapter(movieAdapter);
        }
        
        // Step 1
        rvShowtimes = findViewById(R.id.rv_showtimes);
        progressShowtimes = findViewById(R.id.progress_showtimes);
        tvShowtimesEmpty = findViewById(R.id.tv_showtimes_empty);
        
        if (rvShowtimes != null) {
            rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
            showtimeAdapter = new BookingShowtimeAdapter(new ArrayList<>(), this::onShowtimeClick);
            rvShowtimes.setAdapter(showtimeAdapter);
        }
    }

    private void setupActions() {
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                renderStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!validateCurrentStep()) {
                return;
            }
            
            if (currentStep == 2) {
                createBookingFromSeats();
                return;
            }
            
            if (currentStep == 3) {
                confirmFoodOrder();
                return;
            }
            
            if (currentStep < TOTAL_STEPS - 1) {
                currentStep++;
                renderStep();
            } else {
                finish();
            }
        });

        btnRestartFlow.setOnClickListener(v -> resetFlow());
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0:
                if (selectedMovieId == null) {
                    Toast.makeText(this, "Vui lòng chọn phim", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 1:
                if (selectedShowtimeId == null) {
                    Toast.makeText(this, "Vui lòng chọn suất chiếu", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 2:
                if (selectedSeats.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một ghế", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }
        return true;
    }

    private void resetFlow() {
        currentStep = 0;
        selectedMovieId = null;
        selectedShowtimeId = null;
        selectedShowtime = null;
        selectedSeats.clear();
        currentBookingId = null;
        currentBooking = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopPolling();
        renderStep();
    }

    private void onMovieClick(Movie movie) {
        if (movie != null && movie.getId() != null) {
            selectedMovieId = movie.getId();
            movieAdapter.setSelectedMovieId(movie.getId());
            Log.d(TAG, "Selected movie: " + selectedMovieId);
            Toast.makeText(this, "Đã chọn: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onShowtimeClick(Long showtimeId) {
        selectedShowtimeId = showtimeId;
        showtimeAdapter.setSelectedShowtimeId(showtimeId);
        Log.d(TAG, "Selected showtime: " + showtimeId);
    }

    private void renderStep() {
        for (int i = 0; i < stepContainers.length; i++) {
            stepContainers[i].setVisibility(i == currentStep ? View.VISIBLE : View.GONE);
        }

        for (int i = 0; i < stepIndicators.length; i++) {
            if (i < currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_done, android.R.color.white);
            } else if (i == currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_active, android.R.color.white);
            } else {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_idle, android.R.color.darker_gray);
            }
        }

        btnPrevious.setEnabled(currentStep > 0);
        btnPrevious.setAlpha(currentStep > 0 ? 1f : 0.5f);

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("Đóng");
        } else {
            btnNext.setText("Tiếp tục");
        }
        
        switch (currentStep) {
            case 0:
                loadMovies();
                break;
            case 1:
                if (selectedMovieId != null) {
                    loadShowtimesForMovie(selectedMovieId);
                }
                break;
            case 2:
                initSeatUI();
                if (selectedShowtimeId != null) {
                    loadSeats();
                }
                break;
            case 3:
                if (currentBooking != null) {
                    initFoodUI();
                    loadFoodItems();
                    loadFoodCombos();
                    loadAndShowUserPoints();
                    if (currentBooking.getRemainingMinutes() != null) {
                        startCountdownTimer(currentBooking.getRemainingMinutes() * 60 * 1000L);
                    }
                }
                break;
            case 4:
                initPaymentUI();
                break;
            case 5:
                showCompletionUI();
                break;
        }
    }

    private void applyIndicatorStyle(TextView indicator, int backgroundRes, int textColorRes) {
        indicator.setBackgroundResource(backgroundRes);
        indicator.setTextColor(ContextCompat.getColor(this, textColorRes));
    }

    // ===== STEP 0: Movies =====
    private void loadMovies() {
        loadMovies(0);
    }

    private void loadMovies(int retryIndex) {
        if (progressMovies != null) progressMovies.setVisibility(View.VISIBLE);
        if (rvMovies != null) rvMovies.setVisibility(View.GONE);
        if (tvMoviesEmpty != null) tvMoviesEmpty.setVisibility(View.GONE);

        int size = UPCOMING_PAGE_SIZES[Math.min(retryIndex, UPCOMING_PAGE_SIZES.length - 1)];
        apiService.getUpcomingMovies(0, size).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressMovies != null) progressMovies.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    PageResponse<Movie> pageResponse = response.body().getResult();
                    List<Movie> movies = pageResponse.getMovies();
                    if (movies == null || movies.isEmpty()) {
                        if (tvMoviesEmpty != null) tvMoviesEmpty.setVisibility(View.VISIBLE);
                        if (movieAdapter != null) movieAdapter.updateData(new ArrayList<>());
                    } else {
                        if (rvMovies != null) rvMovies.setVisibility(View.VISIBLE);
                        if (movieAdapter != null) movieAdapter.updateData(movies);
                    }
                } else {
                    if (tvMoviesEmpty != null) tvMoviesEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (t instanceof EOFException && retryIndex < UPCOMING_PAGE_SIZES.length - 1) {
                    Log.w(TAG, "EOF, retrying...");
                    loadMovies(retryIndex + 1);
                    return;
                }
                if (progressMovies != null) progressMovies.setVisibility(View.GONE);
                if (tvMoviesEmpty != null) tvMoviesEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "loadMovies failed", t);
            }
        });
    }

    // ===== STEP 1: Showtimes =====
    private void loadShowtimesForMovie(Long movieId) {
        if (progressShowtimes != null) progressShowtimes.setVisibility(View.VISIBLE);
        if (rvShowtimes != null) rvShowtimes.setVisibility(View.GONE);
        if (tvShowtimesEmpty != null) tvShowtimesEmpty.setVisibility(View.GONE);

        apiService.getMovieShowtimes(movieId).enqueue(new Callback<ApiResponse<List<ShowtimeGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShowtimeGroup>>> call, Response<ApiResponse<List<ShowtimeGroup>>> response) {
                if (progressShowtimes != null) progressShowtimes.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShowtimeGroup> groups = response.body().getResult();
                    if (groups == null || groups.isEmpty()) {
                        if (tvShowtimesEmpty != null) tvShowtimesEmpty.setVisibility(View.VISIBLE);
                        if (showtimeAdapter != null) showtimeAdapter.updateGroupData(new ArrayList<>());
                    } else {
                        if (rvShowtimes != null) rvShowtimes.setVisibility(View.VISIBLE);
                        if (showtimeAdapter != null) showtimeAdapter.updateGroupData(groups);
                    }
                } else {
                    if (tvShowtimesEmpty != null) tvShowtimesEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ShowtimeGroup>>> call, Throwable t) {
                if (progressShowtimes != null) progressShowtimes.setVisibility(View.GONE);
                if (tvShowtimesEmpty != null) tvShowtimesEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "loadShowtimesForMovie failed", t);
            }
        });
    }

    // ===== STEP 2: Seats =====
    private void initSeatUI() {
        View seatView = stepContainers[2];
        // Step container is now ScrollView, get the LinearLayout inside
        if (!(seatView instanceof android.widget.ScrollView)) return;
        android.widget.ScrollView scrollView = (android.widget.ScrollView) seatView;
        if (scrollView.getChildCount() == 0) return;
        
        View childView = scrollView.getChildAt(0);
        if (!(childView instanceof LinearLayout)) return;
        LinearLayout seatContainer = (LinearLayout) childView;
        if (seatContainer.getChildCount() <= 1) return;
        
        View existingCard = seatContainer.getChildAt(1);
        if (existingCard instanceof CardView) {
            CardView card = (CardView) existingCard;
            LinearLayout cardContent = (LinearLayout) card.getChildAt(0);
            
            tvSelectedSeatsInfo = cardContent.findViewWithTag("seats_info");
            tvSeatTotalPrice = cardContent.findViewWithTag("seat_price");
            progressSeats = cardContent.findViewWithTag("progress_seats");
            tvSeatsEmpty = cardContent.findViewWithTag("seats_empty");
            rvSeats = cardContent.findViewWithTag("rv_seats");
            
            if (rvSeats != null) {
                rvSeats.setLayoutManager(new LinearLayoutManager(this));
                seatAdapter = new SeatAdapter(new ArrayList<>(), this::onSeatSelectionChanged);
                rvSeats.setAdapter(seatAdapter);
            }
        }
    }

    private void loadSeats() {
        if (progressSeats == null) return;
        progressSeats.setVisibility(View.VISIBLE);
        if (rvSeats != null) rvSeats.setVisibility(View.GONE);
        if (tvSeatsEmpty != null) tvSeatsEmpty.setVisibility(View.GONE);

        Long showtimeDetailId = selectedShowtime != null ? selectedShowtime.getShowtimeDetailId() : null;
        Long roomId = selectedShowtime != null ? selectedShowtime.getRoomId() : null;

        if (showtimeDetailId != null) {
            apiService.getSeatsForShowtimeDetail(showtimeDetailId)
                    .enqueue(new Callback<ApiResponse<SeatResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<SeatResponse>> call,
                                               Response<ApiResponse<SeatResponse>> response) {
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
                            Log.e(TAG, "getSeatsForShowtimeDetail failed", t);
                            onSeatsEmpty("Lỗi kết nối");
                        }
                    });
            return;
        }

        if (roomId != null) {
            apiService.getSeatsByRoom(roomId).enqueue(new Callback<ApiResponse<List<Seat>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Seat>>> call,
                                       Response<ApiResponse<List<Seat>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Seat> seats = response.body().getResult();
                        if (seats != null && !seats.isEmpty()) {
                            onSeatsLoaded(seats);
                            return;
                        }
                    }
                    onSeatsEmpty("Phòng chiếu chưa có ghế");
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Seat>>> call, Throwable t) {
                    Log.e(TAG, "getSeatsByRoom failed", t);
                    onSeatsEmpty("Lỗi kết nối");
                }
            });
        } else {
            onSeatsEmpty("Không tìm thấy thông tin phòng");
        }
    }

    private void onSeatsLoaded(List<Seat> seats) {
        if (progressSeats != null) progressSeats.setVisibility(View.GONE);
        if (tvSeatsEmpty != null) tvSeatsEmpty.setVisibility(View.GONE);
        if (rvSeats != null) {
            rvSeats.setVisibility(View.VISIBLE);
            seatAdapter.updateSeats(seats);
        }
        onSeatSelectionChanged();
    }

    private void onSeatsEmpty(String message) {
        if (progressSeats != null) progressSeats.setVisibility(View.GONE);
        if (rvSeats != null) rvSeats.setVisibility(View.GONE);
        if (tvSeatsEmpty != null) {
            tvSeatsEmpty.setVisibility(View.VISIBLE);
            tvSeatsEmpty.setText(message);
        }
    }

    private void onSeatSelectionChanged() {
        selectedSeats = seatAdapter.getSelectedSeats();
        if (tvSelectedSeatsInfo == null) return;

        if (selectedSeats.isEmpty()) {
            tvSelectedSeatsInfo.setText("Chưa chọn ghế");
            if (tvSeatTotalPrice != null) tvSeatTotalPrice.setText("0 VNĐ");
        } else {
            String seatLabels = selectedSeats.stream()
                    .map(s -> (s.getRowNumber() != null ? s.getRowNumber() : "") +
                             (s.getSeatNumber() != null ? s.getSeatNumber() : ""))
                    .collect(Collectors.joining(", "));
            tvSelectedSeatsInfo.setText("Ghế: " + seatLabels);

            double totalPrice = selectedSeats.stream()
                    .mapToDouble(s -> s.getPrice() != null ? s.getPrice() : 0.0)
                    .sum();
            if (tvSeatTotalPrice != null) {
                tvSeatTotalPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            }
        }
    }

    private void createBookingFromSeats() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressSeats != null) progressSeats.setVisibility(View.VISIBLE);

        List<Long> seatIds = selectedSeats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());

        BookingRequest request = new BookingRequest(selectedShowtimeId, seatIds);

        apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                        currentBooking = apiResponse.getResult();
                        currentBookingId = resolveBookingId(currentBooking);
                        if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                        Toast.makeText(EmployeeBookingFlowActivity.this,
                                "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                        currentStep = 3;
                        renderStep();
                    } else {
                        if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                        Toast.makeText(EmployeeBookingFlowActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đặt vé thất bại",
                                Toast.LENGTH_LONG).show();
                    }
                } else if (response.code() == 409) {
                    cancelAndRetry(request);
                } else {
                    if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                    Toast.makeText(EmployeeBookingFlowActivity.this,
                            "Đặt vé thất bại", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                Log.e(TAG, "createBooking failed", t);
                Toast.makeText(EmployeeBookingFlowActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelAndRetry(BookingRequest request) {
        apiService.cancelPendingBooking().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Booking>> call,
                                               Response<ApiResponse<Booking>> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getResult() != null) {
                                currentBooking = response.body().getResult();
                                currentBookingId = resolveBookingId(currentBooking);
                                if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                                currentStep = 3;
                                renderStep();
                            } else {
                                if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                                Toast.makeText(EmployeeBookingFlowActivity.this,
                                        "Đặt vé thất bại", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                            if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                            Toast.makeText(EmployeeBookingFlowActivity.this,
                                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                    Toast.makeText(EmployeeBookingFlowActivity.this,
                            "Không thể hủy booking cũ", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                if (progressSeats != null) progressSeats.setVisibility(View.GONE);
                Toast.makeText(EmployeeBookingFlowActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== STEP 3: Food =====
    private void initFoodUI() {
        View foodView = stepContainers[3];
        // Step container is now ScrollView, get the LinearLayout inside
        if (!(foodView instanceof android.widget.ScrollView)) return;
        android.widget.ScrollView scrollView = (android.widget.ScrollView) foodView;
        if (scrollView.getChildCount() == 0) return;
        
        View childView = scrollView.getChildAt(0);
        if (!(childView instanceof LinearLayout)) return;
        LinearLayout foodContainer = (LinearLayout) childView;
        foodContainer.removeAllViews();
        
        // Countdown
        tvCountdown = new TextView(this);
        tvCountdown.setText("Thời gian: --:--");
        tvCountdown.setTextColor(0xFFDC2626);
        tvCountdown.setTextSize(14);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p1.bottomMargin = 12;
        tvCountdown.setLayoutParams(p1);
        foodContainer.addView(tvCountdown);
        
        // Food items
        rvFoodItems = new RecyclerView(this);
        rvFoodItems.setLayoutManager(new LinearLayoutManager(this));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p2.bottomMargin = 12;
        rvFoodItems.setLayoutParams(p2);
        foodContainer.addView(rvFoodItems);
        
        // Food combos
        rvFoodCombos = new RecyclerView(this);
        rvFoodCombos.setLayoutManager(new LinearLayoutManager(this));
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p3.bottomMargin = 12;
        rvFoodCombos.setLayoutParams(p3);
        foodContainer.addView(rvFoodCombos);
        
        // Phone
        etPhone = new EditText(this);
        etPhone.setHint("Số điện thoại");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p4.bottomMargin = 8;
        etPhone.setLayoutParams(p4);
        foodContainer.addView(etPhone);
        
        // Promo
        etPromotionId = new EditText(this);
        etPromotionId.setHint("Mã khuyến mãi");
        LinearLayout.LayoutParams p5 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p5.bottomMargin = 8;
        etPromotionId.setLayoutParams(p5);
        foodContainer.addView(etPromotionId);
        
        // Coupon
        etCouponCode = new EditText(this);
        etCouponCode.setHint("Mã phiếu");
        LinearLayout.LayoutParams p6 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p6.bottomMargin = 8;
        etCouponCode.setLayoutParams(p6);
        foodContainer.addView(etCouponCode);
        
        // Points info
        tvUserPoints = new TextView(this);
        tvUserPoints.setText("Điểm: 0");
        tvUserPoints.setTextSize(12);
        LinearLayout.LayoutParams p7 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p7.bottomMargin = 8;
        tvUserPoints.setLayoutParams(p7);
        foodContainer.addView(tvUserPoints);
        
        // Points input
        etPoints = new EditText(this);
        etPoints.setHint("Điểm để dùng");
        etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams p8 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p8.bottomMargin = 12;
        etPoints.setLayoutParams(p8);
        foodContainer.addView(etPoints);
        
        // Total
        tvFoodTotal = new TextView(this);
        tvFoodTotal.setText("Tổng: 0 VNĐ");
        tvFoodTotal.setTextColor(0xFF059669);
        tvFoodTotal.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p9 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvFoodTotal.setLayoutParams(p9);
        foodContainer.addView(tvFoodTotal);
        
        // Progress
        progressFood = new ProgressBar(this);
        progressFood.setVisibility(View.VISIBLE);
        foodContainer.addView(progressFood);
    }

    private void loadFoodItems() {
        apiService.getAllFoodItems().enqueue(new Callback<ApiResponse<List<FoodItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodItem>>> call, Response<ApiResponse<List<FoodItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodItems = response.body().getResult();
                    if (foodItems != null && !foodItems.isEmpty()) {
                        foodItemAdapter = new FoodAdapter<>(foodItems,
                                new FoodAdapter.FoodItemBinder<FoodItem>() {
                                    @Override public Long getId(FoodItem i) { return i.getFoodItemId(); }
                                    @Override public String getName(FoodItem i) { return i.getName(); }
                                    @Override public Double getPrice(FoodItem i) { return i.getPrice(); }
                                    @Override public String getImageUrl(FoodItem i) { return i.getImageUrl(); }
                                    @Override public String getDescription(FoodItem i) { return null; }
                                    @Override public int getQuantity(FoodItem i) { return i.getQuantity(); }
                                    @Override public void setQuantity(FoodItem i, int q) { i.setQuantity(q); }
                                }, EmployeeBookingFlowActivity.this::updateFoodSummary);
                        rvFoodItems.setAdapter(foodItemAdapter);
                    }
                    if (progressFood != null) progressFood.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FoodItem>>> call, Throwable t) {
                Log.e(TAG, "Failed to load food items", t);
                if (progressFood != null) progressFood.setVisibility(View.GONE);
            }
        });
    }

    private void loadFoodCombos() {
        apiService.getAllFoodCombos().enqueue(new Callback<ApiResponse<List<FoodCombo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodCombo>>> call, Response<ApiResponse<List<FoodCombo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodCombos = response.body().getResult();
                    if (foodCombos != null && !foodCombos.isEmpty()) {
                        foodComboAdapter = new FoodAdapter<>(foodCombos,
                                new FoodAdapter.FoodItemBinder<FoodCombo>() {
                                    @Override public Long getId(FoodCombo c) { return c.getComboId(); }
                                    @Override public String getName(FoodCombo c) { return c.getName(); }
                                    @Override public Double getPrice(FoodCombo c) { return c.getPrice(); }
                                    @Override public String getImageUrl(FoodCombo c) { return c.getImageUrl(); }
                                    @Override public String getDescription(FoodCombo c) { return c.getDescription(); }
                                    @Override public int getQuantity(FoodCombo c) { return c.getQuantity(); }
                                    @Override public void setQuantity(FoodCombo c, int q) { c.setQuantity(q); }
                                }, EmployeeBookingFlowActivity.this::updateFoodSummary);
                        rvFoodCombos.setAdapter(foodComboAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FoodCombo>>> call, Throwable t) {
                Log.e(TAG, "Failed to load food combos", t);
            }
        });
    }

    private void loadAndShowUserPoints() {
        apiService.getMyLoyalty().enqueue(new Callback<ApiResponse<LoyaltyResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoyaltyResponse>> call,
                                   Response<ApiResponse<LoyaltyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoyaltyResponse loyalty = response.body().getResult();
                    if (loyalty != null && tvUserPoints != null) {
                        tvUserPoints.setText("Điểm: " + (loyalty.getTotalPoints() != null ? loyalty.getTotalPoints() : 0));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoyaltyResponse>> call, Throwable t) {
                Log.e(TAG, "Failed to load loyalty", t);
            }
        });
    }

    private void updateFoodSummary() {
        if (tvFoodTotal == null) return;
        double foodTotal = 0;
        for (FoodItem item : foodItems) {
            if (item.getQuantity() > 0) {
                foodTotal += (item.getPrice() != null ? item.getPrice() : 0) * item.getQuantity();
            }
        }
        for (FoodCombo combo : foodCombos) {
            if (combo.getQuantity() > 0) {
                foodTotal += (combo.getPrice() != null ? combo.getPrice() : 0) * combo.getQuantity();
            }
        }
        double seatPrice = currentBooking != null && currentBooking.getTotalPrice() != null 
                ? currentBooking.getTotalPrice() : 0;
        tvFoodTotal.setText("Tổng: " + CurrencyUtils.formatPrice(seatPrice + foodTotal));
    }

    private void confirmFoodOrder() {
        String phone = etPhone.getText().toString().trim();
        String promoCode = etPromotionId.getText().toString().trim();
        String coupon = etCouponCode.getText().toString().trim();
        String pointsStr = etPoints.getText().toString().trim();
        Integer points = pointsStr.isEmpty() ? null : Integer.parseInt(pointsStr);
        Long promoId = promoCode.isEmpty() ? null : Long.parseLong(promoCode);
        
        // Food order
        List<FoodOrderRequest.FoodItemOrder> itemOrders = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.getQuantity() > 0) {
                itemOrders.add(new FoodOrderRequest.FoodItemOrder(
                        item.getFoodItemId(), item.getQuantity()));
            }
        }
        
        List<FoodOrderRequest.FoodComboOrder> comboOrders = new ArrayList<>();
        for (FoodCombo combo : foodCombos) {
            if (combo.getQuantity() > 0) {
                comboOrders.add(new FoodOrderRequest.FoodComboOrder(
                        combo.getComboId(), combo.getQuantity()));
            }
        }
        
        FoodOrderRequest foodOrder = new FoodOrderRequest(itemOrders, comboOrders);
        
        if (!itemOrders.isEmpty() || !comboOrders.isEmpty()) {
            apiService.createFoodOnlyBooking(currentBookingId, foodOrder)
                    .enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                confirmBooking(phone, promoId, coupon, points);
                            } else {
                                Toast.makeText(EmployeeBookingFlowActivity.this,
                                        "Lỗi đặt thức ăn", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            Toast.makeText(EmployeeBookingFlowActivity.this,
                                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            confirmBooking(phone, promoId, coupon, points);
        }
    }

    private void confirmBooking(String phone, Long promoId, String coupon, Integer points) {
        apiService.confirmBookingWithParams(phone, promoId, coupon, points)
                .enqueue(new Callback<ApiResponse<Booking>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentBooking = response.body().getResult();
                            stopCountdownTimer();
                            currentStep = 4;
                            renderStep();
                        } else {
                            Toast.makeText(EmployeeBookingFlowActivity.this,
                                    "Lỗi xác nhận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                        Toast.makeText(EmployeeBookingFlowActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ===== STEP 4: Payment =====
    private void initPaymentUI() {
        View paymentView = stepContainers[4];
        // Step container is now ScrollView, get the LinearLayout inside
        if (!(paymentView instanceof android.widget.ScrollView)) return;
        android.widget.ScrollView scrollView = (android.widget.ScrollView) paymentView;
        if (scrollView.getChildCount() == 0) return;
        
        View childView = scrollView.getChildAt(0);
        if (!(childView instanceof LinearLayout)) return;
        LinearLayout paymentContainer = (LinearLayout) childView;
        paymentContainer.removeAllViews();
        
        // Booking info
        tvPaymentBookingInfo = new TextView(this);
        tvPaymentBookingInfo.setText("Chi tiết");
        tvPaymentBookingInfo.setTextSize(13);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p1.bottomMargin = 12;
        tvPaymentBookingInfo.setLayoutParams(p1);
        paymentContainer.addView(tvPaymentBookingInfo);
        
        // Bank button
        btnPayBank = new Button(this);
        btnPayBank.setText("Chuyển Khoản (VietQR)");
        btnPayBank.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                64
        );
        p2.bottomMargin = 12;
        btnPayBank.setLayoutParams(p2);
        btnPayBank.setOnClickListener(v -> initiateVietQRPayment());
        paymentContainer.addView(btnPayBank);
        
        // Cash section
        TextView cashLabel = new TextView(this);
        cashLabel.setText("Thanh toán tiền mặt");
        cashLabel.setTextSize(13);
        cashLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p3.bottomMargin = 8;
        cashLabel.setLayoutParams(p3);
        paymentContainer.addView(cashLabel);
        
        etCashAmount = new EditText(this);
        etCashAmount.setHint("Nhập số tiền (VNĐ)");
        etCashAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p4.bottomMargin = 8;
        etCashAmount.setLayoutParams(p4);
        paymentContainer.addView(etCashAmount);
        
        btnPayCash = new Button(this);
        btnPayCash.setText("Xác nhận tiền mặt");
        btnPayCash.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p5 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                64
        );
        p5.bottomMargin = 12;
        btnPayCash.setLayoutParams(p5);
        btnPayCash.setOnClickListener(v -> processCashPayment());
        paymentContainer.addView(btnPayCash);
        
        // QR container (hidden)
        cvQrContainer = new View(this);
        LinearLayout.LayoutParams p6 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cvQrContainer.setLayoutParams(p6);
        cvQrContainer.setVisibility(View.GONE);
        paymentContainer.addView(cvQrContainer);
        
        // QR info
        tvBankInfo = new TextView(this);
        tvBankInfo.setText("Thông tin ngân hàng");
        tvBankInfo.setTextSize(12);
        tvBankInfo.setVisibility(View.GONE);
        LinearLayout.LayoutParams p7 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvBankInfo.setLayoutParams(p7);
        paymentContainer.addView(tvBankInfo);
        
        // QR image
        ivQrCode = new ImageView(this);
        ivQrCode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivQrCode.setVisibility(View.GONE);
        LinearLayout.LayoutParams p8 = new LinearLayout.LayoutParams(300, 300);
        p8.gravity = 16; // CENTER_HORIZONTAL
        ivQrCode.setLayoutParams(p8);
        paymentContainer.addView(ivQrCode);
        
        // Countdown
        tvPaymentCountdown = new TextView(this);
        tvPaymentCountdown.setText("--:--");
        tvPaymentCountdown.setTextColor(0xFFDC2626);
        tvPaymentCountdown.setVisibility(View.GONE);
        LinearLayout.LayoutParams p9 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvPaymentCountdown.setLayoutParams(p9);
        paymentContainer.addView(tvPaymentCountdown);
        
        // Progress
        progressPayment = new ProgressBar(this);
        progressPayment.setVisibility(View.GONE);
        paymentContainer.addView(progressPayment);
        
        displayPaymentInfo();
    }

    private void displayPaymentInfo() {
        if (currentBooking != null && tvPaymentBookingInfo != null) {
            String info = "Mã: " + resolveBookingId(currentBooking) + "\n" +
                    "Ghế: " + getSelectedSeatsLabel() + "\n" +
                    "Tổng: " + CurrencyUtils.formatPrice(currentBooking.getTotalPrice());
            tvPaymentBookingInfo.setText(info);
        }
    }

    private void initiateVietQRPayment() {
        cvQrContainer.setVisibility(View.VISIBLE);
        tvBankInfo.setVisibility(View.VISIBLE);
        ivQrCode.setVisibility(View.VISIBLE);
        tvPaymentCountdown.setVisibility(View.VISIBLE);
        btnPayBank.setEnabled(false);
        btnPayCash.setEnabled(false);
        progressPayment.setVisibility(View.VISIBLE);
        
        apiService.createVietQR(new PaymentRequest(currentBookingId))
                .enqueue(new Callback<ApiResponse<PaymentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaymentResponse>> call,
                                           Response<ApiResponse<PaymentResponse>> response) {
                        progressPayment.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                            PaymentResponse result = response.body().getResult();
                            if (result.getQrData() != null && result.getQrData().getQrDataURL() != null) {
                                Glide.with(EmployeeBookingFlowActivity.this)
                                        .load(result.getQrData().getQrDataURL())
                                        .into(ivQrCode);
                                
                                String bankInfo = "Ngân hàng: " + result.getQrData().getBankName() + "\n" +
                                        "TK: " + result.getQrData().getBankAccount();
                                tvBankInfo.setText(bankInfo);
                            }
                            if (result.getRemainingMinutes() != null) {
                                startPaymentCountdownTimer(result.getRemainingMinutes() * 60 * 1000L);
                            }
                            startPaymentPolling(result.getBookingId());
                        } else {
                            Toast.makeText(EmployeeBookingFlowActivity.this,
                                    "Lỗi tạo QR", Toast.LENGTH_SHORT).show();
                            resetPaymentUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PaymentResponse>> call, Throwable t) {
                        progressPayment.setVisibility(View.GONE);
                        Toast.makeText(EmployeeBookingFlowActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        resetPaymentUI();
                    }
                });
    }

    private void processCashPayment() {
        String cashStr = etCashAmount.getText().toString().trim();
        if (cashStr.isEmpty()) {
            Toast.makeText(this, "Nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Double cashAmount = Double.parseDouble(cashStr);
            progressPayment.setVisibility(View.VISIBLE);
            btnPayCash.setEnabled(false);
            
            apiService.payCash(cashAmount)
                    .enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            progressPayment.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(EmployeeBookingFlowActivity.this,
                                        "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                                currentStep = 5;
                                renderStep();
                            } else {
                                Toast.makeText(EmployeeBookingFlowActivity.this,
                                        "Lỗi thanh toán", Toast.LENGTH_SHORT).show();
                                btnPayCash.setEnabled(true);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            progressPayment.setVisibility(View.GONE);
                            Toast.makeText(EmployeeBookingFlowActivity.this,
                                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            btnPayCash.setEnabled(true);
                        }
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPaymentUI() {
        btnPayBank.setEnabled(true);
        btnPayCash.setEnabled(true);
    }

    private void startPaymentPolling(String bookingId) {
        stopPolling();
        pollingRunnable = () -> checkPaymentStatus(bookingId);
        pollingHandler.postDelayed(pollingRunnable, 3000);
    }

    private void checkPaymentStatus(String bookingId) {
        apiService.checkPaymentStatus(bookingId)
                .enqueue(new Callback<ApiResponse<PaymentStatusResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaymentStatusResponse>> call,
                                           Response<ApiResponse<PaymentStatusResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentStatusResponse status = response.body().getResult();
                            if (status != null && status.getStatus() != null) {
                                if ("PAID".equals(status.getStatus())) {
                                    stopPolling();
                                    currentStep = 5;
                                    renderStep();
                                } else if ("CANCELLED".equals(status.getStatus()) || "EXPIRED".equals(status.getStatus())) {
                                    stopPolling();
                                    Toast.makeText(EmployeeBookingFlowActivity.this,
                                            "Thanh toán " + status.getStatus(), Toast.LENGTH_SHORT).show();
                                    resetPaymentUI();
                                } else {
                                    pollingHandler.postDelayed(pollingRunnable, 3000);
                                }
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

    private void stopPolling() {
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingRunnable = null;
        }
    }

    private void startPaymentCountdownTimer(long ms) {
        stopPaymentCountdownTimer();
        countDownTimer = new CountDownTimer(ms, 1000) {
            @Override
            public void onTick(long remaining) {
                long mins = remaining / 1000 / 60;
                long secs = (remaining / 1000) % 60;
                if (tvPaymentCountdown != null) {
                    tvPaymentCountdown.setText(String.format("%d:%02d", mins, secs));
                }
            }

            @Override
            public void onFinish() {
                if (tvPaymentCountdown != null) {
                    tvPaymentCountdown.setText("Hết hạn");
                }
                stopPolling();
            }
        };
        countDownTimer.start();
    }

    private void stopPaymentCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    // ===== STEP 5: Completion =====
    private void showCompletionUI() {
        View completionView = stepContainers[5];
        // Step container is now ScrollView, get the LinearLayout inside
        if (!(completionView instanceof android.widget.ScrollView)) return;
        android.widget.ScrollView scrollView = (android.widget.ScrollView) completionView;
        if (scrollView.getChildCount() == 0) return;
        
        View childView = scrollView.getChildAt(0);
        if (!(childView instanceof LinearLayout)) return;
        LinearLayout completionContainer = (LinearLayout) childView;
        completionContainer.removeAllViews();
        
        // Success
        TextView successText = new TextView(this);
        successText.setText("✓ Đặt vé thành công!");
        successText.setTextSize(20);
        successText.setTypeface(null, android.graphics.Typeface.BOLD);
        successText.setTextColor(0xFF059669);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p1.bottomMargin = 16;
        successText.setLayoutParams(p1);
        completionContainer.addView(successText);
        
        // Summary
        tvCompletionSummary = new TextView(this);
        tvCompletionSummary.setText("Chi tiết");
        tvCompletionSummary.setTextSize(13);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p2.bottomMargin = 16;
        tvCompletionSummary.setLayoutParams(p2);
        completionContainer.addView(tvCompletionSummary);
        
        // New booking button
        btnNewBooking = new Button(this);
        btnNewBooking.setText("Đặt Vé Mới");
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                64
        );
        p3.bottomMargin = 8;
        btnNewBooking.setLayoutParams(p3);
        btnNewBooking.setOnClickListener(v -> resetFlow());
        completionContainer.addView(btnNewBooking);
        
        // Close button
        btnClose = new Button(this);
        btnClose.setText("Đóng");
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                64
        );
        btnClose.setLayoutParams(p4);
        btnClose.setOnClickListener(v -> finish());
        completionContainer.addView(btnClose);
        
        displayCompletionSummary();
    }

    private void displayCompletionSummary() {
        if (currentBooking != null && tvCompletionSummary != null) {
            String info = "Mã: " + resolveBookingId(currentBooking) + "\n" +
                    "Ghế: " + getSelectedSeatsLabel() + "\n" +
                    "Tổng: " + CurrencyUtils.formatPrice(currentBooking.getTotalPrice()) + "\n" +
                    "Trạng thái: " + (currentBooking.getStatus() != null ? currentBooking.getStatus() : "OK");
            tvCompletionSummary.setText(info);
        }
    }

    // ===== Utilities =====
    private String resolveBookingId(Booking booking) {
        return booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
    }

    private String getSelectedSeatsLabel() {
        return selectedSeats.stream()
                .map(s -> (s.getRowNumber() != null ? s.getRowNumber() : "") +
                         (s.getSeatNumber() != null ? s.getSeatNumber() : ""))
                .collect(Collectors.joining(", "));
    }

    private void startCountdownTimer(long ms) {
        stopCountdownTimer();
        countDownTimer = new CountDownTimer(ms, 1000) {
            @Override
            public void onTick(long remaining) {
                long mins = remaining / 1000 / 60;
                long secs = (remaining / 1000) % 60;
                if (tvCountdown != null) {
                    tvCountdown.setText(String.format("%d:%02d", mins, secs));
                }
            }

            @Override
            public void onFinish() {
                if (tvCountdown != null) tvCountdown.setText("Hết hạn");
            }
        };
        countDownTimer.start();
    }

    private void stopCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
