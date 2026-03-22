package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.FoodAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.FoodCombo;
import com.example.lab10.models.FoodItem;
import com.example.lab10.models.FoodOrderRequest;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodOrderActivity extends AppCompatActivity {

    private static final String TAG = "FoodOrder";

    // Views
    private ImageView ivBack;
    private TextView tvMovieTitle, tvSeatsInfo, tvSeatPrice,
            tvCountdown, tvFoodTotal, tvFinalTotal;
    private RecyclerView rvFoodItems, rvFoodCombos;
    private EditText etPhone, etPromotionId, etCouponCode, etPoints;
    private TextView tvUserPoints;
    private android.view.View cardPoints;
    private Button btnConfirm, btnCancel;
    private ProgressBar progressBar;

    // Data
    private String bookingId;
    private String movieTitle;
    private String seatsInfo;
    private double seatPrice;
    private int remainingMinutes;
    private Showtime showtime;
    private CountDownTimer countDownTimer;

    private List<FoodItem> foodItems = new ArrayList<>();
    private List<FoodCombo> foodCombos = new ArrayList<>();
    private FoodAdapter<FoodItem> foodItemAdapter;
    private FoodAdapter<FoodCombo> foodComboAdapter;

    private MovieApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_order);

        sessionManager = new SessionManager(this);
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }
        apiService = ApiClient.getApiService();

        // Nhận data từ SeatSelectionActivity
        bookingId       = getIntent().getStringExtra("BOOKING_ID");
        movieTitle      = getIntent().getStringExtra("MOVIE_TITLE");
        seatsInfo       = getIntent().getStringExtra("SEATS_INFO");
        seatPrice       = getIntent().getDoubleExtra("SEAT_PRICE", 0.0);
        remainingMinutes = getIntent().getIntExtra("REMAINING_MINUTES", 2);
        showtime        = (Showtime) getIntent().getSerializableExtra(
                SeatSelectionActivity.EXTRA_SHOWTIME);

        initViews();
        displayBookingInfo();
        loadFoodItems();
        loadFoodCombos();
        if (bookingId != null) {
            startCountdown(remainingMinutes * 60 * 1000L);
        }
    }

    private void initViews() {
        ivBack       = findViewById(R.id.iv_back);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvSeatsInfo  = findViewById(R.id.tv_seats_info);
        tvSeatPrice  = findViewById(R.id.tv_seat_price);
        tvCountdown  = findViewById(R.id.tv_countdown);
        tvFoodTotal  = findViewById(R.id.tv_food_total);
        tvFinalTotal = findViewById(R.id.tv_final_total);
        rvFoodItems  = findViewById(R.id.rv_food_items);
        rvFoodCombos = findViewById(R.id.rv_food_combos);
        etPhone        = findViewById(R.id.et_phone);
        etPromotionId  = findViewById(R.id.et_promotion_id);
        etCouponCode   = findViewById(R.id.et_coupon_code);
        etPoints       = findViewById(R.id.et_points);
        tvUserPoints   = findViewById(R.id.tv_user_points);
        cardPoints     = findViewById(R.id.card_points);

        // Hiện điểm user khi load
        loadAndShowUserPoints();
        btnConfirm   = findViewById(R.id.btn_confirm);
        btnCancel    = findViewById(R.id.btn_cancel);
        progressBar  = findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(v -> finish());

        if (bookingId == null) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Cần đặt vé trước");
            btnCancel.setVisibility(View.GONE);
            if (tvCountdown != null) {
                tvCountdown.setText("Vui lòng đặt vé trước khi đặt đồ ăn");
            }
        } else {
            btnConfirm.setOnClickListener(v -> onConfirmClicked());
            btnCancel.setOnClickListener(v -> cancelBooking());
        }

        rvFoodItems.setLayoutManager(new LinearLayoutManager(this));
        rvFoodCombos.setLayoutManager(new LinearLayoutManager(this));

        foodItemAdapter = new FoodAdapter<>(foodItems,
                new FoodAdapter.FoodItemBinder<FoodItem>() {
                    @Override public Long getId(FoodItem i) { return i.getFoodItemId(); }
                    @Override public String getName(FoodItem i) { return i.getName(); }
                    @Override public Double getPrice(FoodItem i) { return i.getPrice(); }
                    @Override public String getImageUrl(FoodItem i) { return i.getImageUrl(); }
                    @Override public String getDescription(FoodItem i) { return null; }
                    @Override public int getQuantity(FoodItem i) { return i.getQuantity(); }
                    @Override public void setQuantity(FoodItem i, int q) { i.setQuantity(q); }
                }, this::updateSummary);
        rvFoodItems.setAdapter(foodItemAdapter);

        foodComboAdapter = new FoodAdapter<>(foodCombos,
                new FoodAdapter.FoodItemBinder<FoodCombo>() {
                    @Override public Long getId(FoodCombo c) { return c.getComboId(); }
                    @Override public String getName(FoodCombo c) { return c.getName(); }
                    @Override public Double getPrice(FoodCombo c) { return c.getPrice(); }
                    @Override public String getImageUrl(FoodCombo c) { return c.getImageUrl(); }
                    @Override public String getDescription(FoodCombo c) { return c.getDescription(); }
                    @Override public int getQuantity(FoodCombo c) { return c.getQuantity(); }
                    @Override public void setQuantity(FoodCombo c, int q) { c.setQuantity(q); }
                }, this::updateSummary);
        rvFoodCombos.setAdapter(foodComboAdapter);
    }

    private void displayBookingInfo() {
        tvMovieTitle.setText(movieTitle != null ? movieTitle : "");
        tvSeatsInfo.setText("Ghế: " + (seatsInfo != null ? seatsInfo : ""));
        tvSeatPrice.setText("Giá vé: " + CurrencyUtils.formatPrice(seatPrice));
        tvFoodTotal.setText("Đồ ăn: " + CurrencyUtils.formatPrice(0.0));
        tvFinalTotal.setText("Tổng: " + CurrencyUtils.formatPrice(seatPrice));
    }

    private void updateSummary() {
        double foodTotal = 0.0;
        for (FoodItem item : foodItems) {
            foodTotal += item.getQuantity() * (item.getPrice() != null ? item.getPrice() : 0);
        }
        for (FoodCombo combo : foodCombos) {
            foodTotal += combo.getQuantity() * (combo.getPrice() != null ? combo.getPrice() : 0);
        }
        tvFoodTotal.setText("Đồ ăn: " + CurrencyUtils.formatPrice(foodTotal));
        tvFinalTotal.setText("Tổng tạm tính: " + CurrencyUtils.formatPrice(seatPrice + foodTotal));
    }

    private void startCountdown(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = millisUntilFinished / 60000;
                long sec = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("Hết hạn sau: %02d:%02d", min, sec));
            }
            @Override
            public void onFinish() {
                tvCountdown.setText("Booking đã hết hạn!");
                btnConfirm.setEnabled(false);
            }
        }.start();
    }

    private void loadFoodItems() {
        apiService.getAllFoodItems().enqueue(new Callback<ApiResponse<List<FoodItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodItem>>> call,
                                   Response<ApiResponse<List<FoodItem>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResult() != null) {
                    foodItems.clear();
                    foodItems.addAll(response.body().getResult());
                    foodItemAdapter.updateItems(foodItems);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<FoodItem>>> call, Throwable t) {
                Toast.makeText(FoodOrderActivity.this,
                        "Lỗi tải món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodCombos() {
        apiService.getAllFoodCombos().enqueue(new Callback<ApiResponse<List<FoodCombo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodCombo>>> call,
                                   Response<ApiResponse<List<FoodCombo>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResult() != null) {
                    foodCombos.clear();
                    foodCombos.addAll(response.body().getResult());
                    foodComboAdapter.updateItems(foodCombos);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<FoodCombo>>> call, Throwable t) {}
        });
    }

    private void onConfirmClicked() {
        List<FoodOrderRequest.FoodItemOrder> itemOrders = foodItems.stream()
                .filter(i -> i.getQuantity() > 0)
                .map(i -> new FoodOrderRequest.FoodItemOrder(i.getFoodItemId(), i.getQuantity()))
                .collect(Collectors.toList());
        List<FoodOrderRequest.FoodComboOrder> comboOrders = foodCombos.stream()
                .filter(c -> c.getQuantity() > 0)
                .map(c -> new FoodOrderRequest.FoodComboOrder(c.getComboId(), c.getQuantity()))
                .collect(Collectors.toList());

        boolean hasFood = !itemOrders.isEmpty() || !comboOrders.isEmpty();

        progressBar.setVisibility(View.VISIBLE);
        btnConfirm.setEnabled(false);

        if (hasFood) {
            // Bước 1: đặt food trước
            FoodOrderRequest foodRequest = new FoodOrderRequest(itemOrders, comboOrders);
            apiService.createFoodOnlyBooking(bookingId, foodRequest)
                    .enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call,
                                               Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                // Bước 2: confirm
                                doConfirm();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnConfirm.setEnabled(true);
                                Toast.makeText(FoodOrderActivity.this,
                                        "Lỗi đặt đồ ăn (lỗi " + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            btnConfirm.setEnabled(true);
                            Toast.makeText(FoodOrderActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Không có food → confirm thẳng
            doConfirm();
        }
    }

    /** Gọi GET /api/loyalty/me → hiển thị điểm + tier */
    private void loadAndShowUserPoints() {
        apiService.getMyLoyalty().enqueue(new Callback<ApiResponse<com.example.lab10.models.LoyaltyInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.lab10.models.LoyaltyInfo>> call,
                                   Response<ApiResponse<com.example.lab10.models.LoyaltyInfo>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResult() != null) {

                    com.example.lab10.models.LoyaltyInfo loyalty = response.body().getResult();
                    int pts = loyalty.getCurrentPoints();

                    if (tvUserPoints != null && cardPoints != null) {
                        // Hiện số điểm
                        String pointsText = pts + " điểm";

                        // Thêm tier nếu có
                        if (loyalty.getTierName() != null && !loyalty.getTierName().isEmpty()) {
                            pointsText += "  •  " + loyalty.getTierName();
                        }

                        // Thêm % giảm giá nếu có
                        if (loyalty.getDiscountPercentage() != null
                                && loyalty.getDiscountPercentage() > 0) {
                            pointsText += "  (−"
                                    + Math.round(loyalty.getDiscountPercentage() * 100) + "%)";
                        }

                        tvUserPoints.setText(pointsText);
                        cardPoints.setVisibility(android.view.View.VISIBLE);

                        // Cập nhật hint ô điểm
                        if (etPoints != null) {
                            if (pts > 0) {
                                etPoints.setHint("Điểm muốn dùng (có " + pts + " điểm)");
                            } else {
                                etPoints.setHint("Điểm muốn dùng (chưa có điểm)");
                            }
                        }
                    }
                }
                // Nếu API lỗi hoặc chưa có loyalty → card ẩn, không hiện gì
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.lab10.models.LoyaltyInfo>> call, Throwable t) {
                // Mạng lỗi → ẩn card
            }
        });
    }

    private void doConfirm() {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            progressBar.setVisibility(View.GONE);
            btnConfirm.setEnabled(true);
            Toast.makeText(this, "Không tìm thấy mã booking", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone    = etPhone.getText().toString().trim();
        String promoStr = etPromotionId.getText().toString().trim();
        String coupon   = etCouponCode.getText().toString().trim();
        String ptStr    = etPoints.getText().toString().trim();

        Long promotionId;
        Integer pointsToUse;
        try {
            promotionId = promoStr.isEmpty() ? null : Long.parseLong(promoStr);
            pointsToUse = ptStr.isEmpty() ? null : Integer.parseInt(ptStr);
        } catch (NumberFormatException e) {
            progressBar.setVisibility(View.GONE);
            btnConfirm.setEnabled(true);
            Toast.makeText(this, "Promotion ID hoặc điểm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String  phoneVal     = phone.isEmpty() ? null : phone;
        String  couponVal    = coupon.isEmpty() ? null : coupon;

        apiService.confirmBookingWithParams(phoneVal, promotionId, couponVal, pointsToUse)
                .enqueue(new Callback<ApiResponse<Booking>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Booking>> call,
                                           Response<ApiResponse<Booking>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Booking confirmedBooking = response.body().getResult();
                            if (countDownTimer != null) countDownTimer.cancel();

                            if (confirmedBooking != null) {
                                // Giữ trạng thái CONFIRMED, không ép sang PAID
                                // Chuyển sang trang xác nhận → user tự chọn thanh toán
                                navigateToConfirmation(confirmedBooking);
                            }
                        } else {
                            btnConfirm.setEnabled(true);
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "";
                                Toast.makeText(FoodOrderActivity.this,
                                        "Xác nhận thất bại: " + err,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception ignored) {
                                Toast.makeText(FoodOrderActivity.this,
                                        "Xác nhận thất bại (lỗi " + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnConfirm.setEnabled(true);
                        Toast.makeText(FoodOrderActivity.this,
                                "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String buildFoodSummary() {
        StringBuilder sb = new StringBuilder();
        for (FoodItem item : foodItems) {
            if (item.getQuantity() > 0) {
                sb.append(item.getName()).append(" x").append(item.getQuantity()).append("\n");
            }
        }
        for (FoodCombo combo : foodCombos) {
            if (combo.getQuantity() > 0) {
                sb.append(combo.getName()).append(" x").append(combo.getQuantity()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void cancelBooking() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Hủy booking này?")
                .setPositiveButton("Hủy vé", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    apiService.cancelPendingBooking()
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call,
                                                       Response<ApiResponse<Object>> response) {
                                    progressBar.setVisibility(View.GONE);
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    Toast.makeText(FoodOrderActivity.this,
                                            "Đã hủy booking", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(FoodOrderActivity.this,
                                            com.example.lab10.MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(FoodOrderActivity.this,
                                            "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Giữ lại", null)
                .show();
    }
    private void navigateToConfirmation(Booking booking) {
        Intent intent = new Intent(FoodOrderActivity.this,
                BookingConfirmationActivity.class);
        intent.putExtra(BookingConfirmationActivity.EXTRA_BOOKING, booking);
        intent.putExtra("FOOD_SUMMARY", buildFoodSummary());
        intent.putExtra("MOVIE_TITLE", movieTitle);
        intent.putExtra("SEATS_INFO", seatsInfo);
        intent.putExtra("SEAT_PRICE", seatPrice);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}