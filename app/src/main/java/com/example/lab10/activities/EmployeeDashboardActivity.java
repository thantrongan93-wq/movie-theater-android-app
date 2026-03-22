package com.example.lab10.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.lab10.R;
import com.example.lab10.adapters.FoodAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.FoodItem;
import com.example.lab10.models.ScanResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.SessionManager;
import com.google.zxing.client.android.Intents;
import com.google.android.material.tabs.TabLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private static final int TAB_SCAN = 0;
    private static final int TAB_ORDER = 1;

    private TabLayout tabLayout;
    private LinearLayout scanContainer;
    private LinearLayout orderContainer;

    // Scan Tab views
    private Button btnScan;
    private ProgressBar scanProgressBar;
    private LinearLayout scanResultLayout;
    private TextView tvBookingDetails;
    private TextView tvCustomerName;
    private TextView tvMovieInfo;
    private TextView tvSeatsInfo;
    private TextView tvTotalPrice;
    private TextView tvCheckInStatus;
    private Button btnConfirmCheckIn;
    private Button btnNewScan;

    // Order Tab views
    private EditText etCustomerName;
    private EditText etCustomerPhone;
    private RecyclerView rvFoodItems;
    private TextView tvOrderTotal;
    private ProgressBar orderProgressBar;
    private Button btnCreateOrder;
    private Button btnResetOrder;

    private SessionManager sessionManager;
    private MovieApiService apiService;
    private int selectedTab = TAB_SCAN;
    private FoodAdapter<FoodItem> foodAdapter;
    private ScanResponse currentScanResult;
    private List<FoodItem> foodItems = new ArrayList<>();
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<ScanOptions> qrScannerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        sessionManager = new SessionManager(this);
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }
        apiService = ApiClient.getApiService();

        if (!isCurrentUserEmployee()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupScanLaunchers();
        initViews();
        setupTabs();
        showTab(TAB_SCAN);
    }

    private void setupScanLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchQrScanner();
                    } else {
                        Toast.makeText(this, "Ứng dụng cần quyền camera để quét QR", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        qrScannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result == null || result.getContents() == null) {
                return;
            }

            String rawContent = result.getContents().trim();
            if (TextUtils.isEmpty(rawContent)) {
                Toast.makeText(this, "Không đọc được mã QR", Toast.LENGTH_SHORT).show();
                return;
            }
            performScan(rawContent);
        });
    }

    private boolean isCurrentUserEmployee() {
        User user = sessionManager.getUser();
        return user != null && user.isEmployee();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nhân viên bán vé");
        }

        tabLayout = findViewById(R.id.tab_layout);
        scanContainer = findViewById(R.id.layout_scan_container);
        orderContainer = findViewById(R.id.layout_order_container);

        // Scan Tab
        btnScan = findViewById(R.id.btn_scan);
        scanProgressBar = findViewById(R.id.progress_scan);
        scanResultLayout = findViewById(R.id.layout_scan_result);
        tvBookingDetails = findViewById(R.id.tv_booking_details);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvMovieInfo = findViewById(R.id.tv_movie_info);
        tvSeatsInfo = findViewById(R.id.tv_seats_info);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvCheckInStatus = findViewById(R.id.tv_checkin_status);
        btnConfirmCheckIn = findViewById(R.id.btn_confirm_checkin);
        btnNewScan = findViewById(R.id.btn_new_scan);

        btnScan.setOnClickListener(v -> startCameraQrScan());
        btnConfirmCheckIn.setOnClickListener(v -> confirmCheckIn());
        btnNewScan.setOnClickListener(v -> {
            resetScanForm();
            startCameraQrScan();
        });

        // Order Tab
        etCustomerName = findViewById(R.id.et_order_customer_name);
        etCustomerPhone = findViewById(R.id.et_order_customer_phone);
        rvFoodItems = findViewById(R.id.rv_order_food_items);
        tvOrderTotal = findViewById(R.id.tv_order_total);
        orderProgressBar = findViewById(R.id.progress_order);
        btnCreateOrder = findViewById(R.id.btn_create_order);
        btnResetOrder = findViewById(R.id.btn_reset_order);

        rvFoodItems.setLayoutManager(new GridLayoutManager(this, 2));
        foodAdapter = new FoodAdapter<>(foodItems,
                new FoodAdapter.FoodItemBinder<FoodItem>() {
                    @Override public Long getId(FoodItem i) { return i.getFoodItemId(); }
                    @Override public String getName(FoodItem i) { return i.getName(); }
                    @Override public Double getPrice(FoodItem i) { return i.getPrice(); }
                    @Override public String getImageUrl(FoodItem i) { return i.getImageUrl(); }
                    @Override public String getDescription(FoodItem i) { return null; }
                    @Override public int getQuantity(FoodItem i) { return i.getQuantity(); }
                    @Override public void setQuantity(FoodItem i, int q) { i.setQuantity(q); }
                }, this::updateOrderTotal);
        rvFoodItems.setAdapter(foodAdapter);

        btnCreateOrder.setOnClickListener(v -> createOrder());
        btnResetOrder.setOnClickListener(v -> resetOrderForm());

        loadFoodItems();
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Quét"), true);
        tabLayout.addTab(tabLayout.newTab().setText("Đặt hàng"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == TAB_SCAN) {
                    resetScanForm();
                } else if (tab.getPosition() == TAB_ORDER) {
                    loadFoodItems();
                }
            }
        });
    }

    private void showTab(int tabIndex) {
        selectedTab = tabIndex;
        if (tabIndex == TAB_SCAN) {
            scanContainer.setVisibility(View.VISIBLE);
            orderContainer.setVisibility(View.GONE);
            resetScanForm();
        } else {
            scanContainer.setVisibility(View.GONE);
            orderContainer.setVisibility(View.VISIBLE);
            loadFoodItems();
        }
    }

    // ============ SCAN TAB METHODS ============
    private void performScan(String rawQrContent) {
        if (TextUtils.isEmpty(rawQrContent)) {
            Toast.makeText(this, "Mã QR không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookingId = rawQrContent.trim();

        scanProgressBar.setVisibility(View.VISIBLE);
        scanResultLayout.setVisibility(View.GONE);

        apiService.scanQrBooking(bookingId).enqueue(new Callback<ApiResponse<ScanResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ScanResponse>> call, Response<ApiResponse<ScanResponse>> response) {
                scanProgressBar.setVisibility(View.GONE);
                ApiResponse<ScanResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    currentScanResult = body.getResult();
                    displayScanResult();
                    scanResultLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(EmployeeDashboardActivity.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ScanResponse>> call, Throwable t) {
                scanProgressBar.setVisibility(View.GONE);
                Toast.makeText(EmployeeDashboardActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCameraQrScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchQrScanner();
            return;
        }
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Đưa mã QR vào khung để quét");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.addExtra(Intents.Scan.FORMATS, ScanOptions.QR_CODE);
        qrScannerLauncher.launch(options);
    }

    private void displayScanResult() {
        if (currentScanResult == null) return;

        tvCustomerName.setText("Khách: " + currentScanResult.getCustomerName());
        tvMovieInfo.setText("Phim: " + currentScanResult.getMovieName() + "\nGiờ chiếu: " + currentScanResult.getShowtime());

        List<String> seats = currentScanResult.getSeats();
        String seatsText = seats != null && !seats.isEmpty() ? String.join(", ", seats) : "N/A";
        tvSeatsInfo.setText("Ghế: " + seatsText);

        Double price = currentScanResult.getTotalPrice();
        tvTotalPrice.setText("Tổng tiền: " + (price != null ? CurrencyUtils.formatPrice(price) : "0 đ"));

        Boolean isCheckedIn = currentScanResult.getIsCheckedIn();
        if (isCheckedIn != null && isCheckedIn) {
            tvCheckInStatus.setText("✓ Đã check-in");
            tvCheckInStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnConfirmCheckIn.setEnabled(false);
        } else {
            tvCheckInStatus.setText("Chưa check-in");
            tvCheckInStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnConfirmCheckIn.setEnabled(true);
        }

        tvBookingDetails.setText("Mã đơn: " + currentScanResult.getBookingId() +
                "\nNgày đặt: " + currentScanResult.getBookingDate() +
                "\nPhòng: " + currentScanResult.getRoom());
    }

    private void confirmCheckIn() {
        if (currentScanResult == null) return;

        btnConfirmCheckIn.setEnabled(false);
        scanProgressBar.setVisibility(View.VISIBLE);

        apiService.checkInBooking(currentScanResult.getBookingId()).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                scanProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EmployeeDashboardActivity.this, "Check-in thành công", Toast.LENGTH_SHORT).show();
                    currentScanResult.setIsCheckedIn(true);
                    currentScanResult.setCheckedInTime(new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
                    displayScanResult();
                } else {
                    Toast.makeText(EmployeeDashboardActivity.this, "Check-in thất bại", Toast.LENGTH_SHORT).show();
                    btnConfirmCheckIn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                scanProgressBar.setVisibility(View.GONE);
                btnConfirmCheckIn.setEnabled(true);
                Toast.makeText(EmployeeDashboardActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetScanForm() {
        scanResultLayout.setVisibility(View.GONE);
        currentScanResult = null;
    }

    // ============ ORDER TAB METHODS ============
    private void loadFoodItems() {
        orderProgressBar.setVisibility(View.VISIBLE);

        apiService.getAllFoodItems().enqueue(new Callback<ApiResponse<List<FoodItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FoodItem>>> call, Response<ApiResponse<List<FoodItem>>> response) {
                orderProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    foodItems.clear();
                    foodItems.addAll(response.body().getResult());
                    foodAdapter.notifyDataSetChanged();
                    updateOrderTotal();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FoodItem>>> call, Throwable t) {
                orderProgressBar.setVisibility(View.GONE);
                Toast.makeText(EmployeeDashboardActivity.this, "Không thể tải danh sách đồ ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrder() {
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();

        if (TextUtils.isEmpty(customerName) || TextUtils.isEmpty(customerPhone)) {
            Toast.makeText(this, "Vui lòng nhập tên và SĐT khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FoodItem> selectedItems = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.getQuantity() > 0) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một mặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        orderProgressBar.setVisibility(View.VISIBLE);
        btnCreateOrder.setEnabled(false);

        // TODO: Create API call for creating order
        Toast.makeText(this, "Tạo đơn hàng cho: " + customerName, Toast.LENGTH_SHORT).show();
        orderProgressBar.setVisibility(View.GONE);
        btnCreateOrder.setEnabled(true);
    }

    private void updateOrderTotal() {
        double total = 0;
        for (FoodItem item : foodItems) {
            if (item.getQuantity() > 0 && item.getPrice() != null) {
                total += item.getQuantity() * item.getPrice();
            }
        }
        tvOrderTotal.setText("Tổng tiền: " + CurrencyUtils.formatPrice(total));
    }

    private void resetOrderForm() {
        etCustomerName.setText("");
        etCustomerPhone.setText("");
        for (FoodItem item : foodItems) {
            item.setQuantity(0);
        }
        foodAdapter.notifyDataSetChanged();
        updateOrderTotal();
    }
}
