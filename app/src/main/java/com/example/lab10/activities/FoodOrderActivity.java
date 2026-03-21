package com.example.lab10.activities;

import android.os.Bundle;
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
import com.example.lab10.adapters.FoodAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.FoodCombo;
import com.example.lab10.models.FoodItem;
import com.example.lab10.models.FoodOrderRequest;
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

    private ImageView ivBack;
    private RecyclerView rvFoodItems, rvFoodCombos;
    private TextView tvSelectedCount, tvTotalPrice;
    private Button btnOrder;
    private ProgressBar progressBar;

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

        initViews();
        loadFoodItems();
        loadFoodCombos();
    }

    private void initViews() {
        ivBack          = findViewById(R.id.iv_back);
        rvFoodItems     = findViewById(R.id.rv_food_items);
        rvFoodCombos    = findViewById(R.id.rv_food_combos);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        tvTotalPrice    = findViewById(R.id.tv_total_price);
        btnOrder        = findViewById(R.id.btn_order);
        progressBar     = findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(v -> finish());
        btnOrder.setOnClickListener(v -> placeOrder());

        rvFoodItems.setLayoutManager(new LinearLayoutManager(this));
        rvFoodCombos.setLayoutManager(new LinearLayoutManager(this));

        // FoodItem adapter
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

        // FoodCombo adapter
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

    private void loadFoodItems() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAllFoodItems().enqueue(new Callback<ApiResponse<List<FoodItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodItem>>> call,
                                   Response<ApiResponse<List<FoodItem>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResult() != null) {
                    foodItems.clear();
                    foodItems.addAll(response.body().getResult());
                    foodItemAdapter.updateItems(foodItems);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FoodItem>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodOrderActivity.this,
                        "Lỗi tải món ăn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<ApiResponse<List<FoodCombo>>> call, Throwable t) {
                Toast.makeText(FoodOrderActivity.this,
                        "Lỗi tải combo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary() {
        int totalCount = 0;
        double totalPrice = 0.0;

        for (FoodItem item : foodItems) {
            totalCount += item.getQuantity();
            totalPrice += item.getQuantity() * (item.getPrice() != null ? item.getPrice() : 0);
        }
        for (FoodCombo combo : foodCombos) {
            totalCount += combo.getQuantity();
            totalPrice += combo.getQuantity() * (combo.getPrice() != null ? combo.getPrice() : 0);
        }

        if (totalCount == 0) {
            tvSelectedCount.setText("Chưa chọn món nào");
            tvTotalPrice.setText("0 VNĐ");
            btnOrder.setEnabled(false);
        } else {
            tvSelectedCount.setText("Đã chọn " + totalCount + " món");
            tvTotalPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            btnOrder.setEnabled(true);
        }
    }

    private void placeOrder() {
        // Build request
        List<FoodOrderRequest.FoodItemOrder> itemOrders = foodItems.stream()
                .filter(i -> i.getQuantity() > 0)
                .map(i -> new FoodOrderRequest.FoodItemOrder(i.getFoodItemId(), i.getQuantity()))
                .collect(Collectors.toList());

        List<FoodOrderRequest.FoodComboOrder> comboOrders = foodCombos.stream()
                .filter(c -> c.getQuantity() > 0)
                .map(c -> new FoodOrderRequest.FoodComboOrder(c.getComboId(), c.getQuantity()))
                .collect(Collectors.toList());

        FoodOrderRequest request = new FoodOrderRequest(itemOrders, comboOrders);

        progressBar.setVisibility(View.VISIBLE);
        btnOrder.setEnabled(false);

        apiService.createFoodOnlyBooking(null, request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call,
                                           Response<ApiResponse<Object>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(FoodOrderActivity.this,
                                    "Đặt đồ ăn thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnOrder.setEnabled(true);
                            Toast.makeText(FoodOrderActivity.this,
                                    "Đặt thất bại (lỗi " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnOrder.setEnabled(true);
                        Toast.makeText(FoodOrderActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}