package com.example.lab10.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.ScanResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

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
    private EditText etBookingCode;
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

    // Booking Flow entry
    private Button btnOpenEmployeeBookingFlow;

    private SessionManager sessionManager;
    private MovieApiService apiService;
    private ScanResponse currentScanResult;
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

            String bookingCode = extractBookingCodeFromQr(rawContent);
            etBookingCode.setText(bookingCode);
            performScan(bookingCode);
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
        etBookingCode = findViewById(R.id.et_booking_code);
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

        btnScan.setOnClickListener(v -> {
            startCameraQrScan();
        });
        btnConfirmCheckIn.setOnClickListener(v -> confirmCheckIn());
        btnNewScan.setOnClickListener(v -> resetScanForm());

        // Booking flow entry in Order tab
        btnOpenEmployeeBookingFlow = findViewById(R.id.btn_open_employee_booking_flow);
        btnOpenEmployeeBookingFlow.setOnClickListener(v -> openEmployeeBookingFlow());
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Quét"), true);
        tabLayout.addTab(tabLayout.newTab().setText("Booking"));
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
                    openEmployeeBookingFlow();
                }
            }
        });
    }

    private void showTab(int tabIndex) {
        if (tabIndex == TAB_SCAN) {
            scanContainer.setVisibility(View.VISIBLE);
            orderContainer.setVisibility(View.GONE);
            resetScanForm();
        } else {
            scanContainer.setVisibility(View.GONE);
            orderContainer.setVisibility(View.VISIBLE);
            openEmployeeBookingFlow();
        }
    }

    private void openEmployeeBookingFlow() {
        startActivity(new Intent(EmployeeDashboardActivity.this, EmployeeBookingFlowActivity.class));
    }

    // ============ SCAN TAB METHODS ============
    private void performScan(String bookingId) {
        if (TextUtils.isEmpty(bookingId)) {
            Toast.makeText(this, "Không lấy được bookingId từ QR", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    String message = body != null && body.getMessage() != null
                            ? body.getMessage() : "Không tìm thấy đơn hàng";
                    Toast.makeText(EmployeeDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private String extractBookingCodeFromQr(String rawContent) {
        String content = rawContent.trim();
        if (TextUtils.isEmpty(content)) {
            return content;
        }

        if (content.startsWith("{")) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                String bookingCode = firstNonEmpty(
                        jsonObject.optString("bookingId"),
                        jsonObject.optString("bookingCode"),
                        jsonObject.optString("code")
                );
                if (!TextUtils.isEmpty(bookingCode)) {
                    return bookingCode;
                }
            } catch (Exception ignored) {
            }
        }

        Uri uri = Uri.parse(content);
        if (uri != null && uri.getScheme() != null) {
            String fromQuery = firstNonEmpty(
                    uri.getQueryParameter("bookingId"),
                    uri.getQueryParameter("bookingCode"),
                    uri.getQueryParameter("code"),
                    uri.getQueryParameter("id")
            );
            if (!TextUtils.isEmpty(fromQuery)) {
                return fromQuery;
            }

            List<String> pathSegments = uri.getPathSegments();
            if (pathSegments != null && !pathSegments.isEmpty()) {
                String lastSegment = pathSegments.get(pathSegments.size() - 1);
                if (!TextUtils.isEmpty(lastSegment)) {
                    return lastSegment;
                }
            }
        }

        return content;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value.trim();
            }
        }
        return null;
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
        etBookingCode.setText("");
        scanResultLayout.setVisibility(View.GONE);
        currentScanResult = null;
        etBookingCode.requestFocus();
    }
}
