package com.example.lab10.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.AdminReportRequest;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String MONTH_PATTERN = "yyyy-MM";
    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final int TAB_DASHBOARD = 0;
    private static final int TAB_MOVIES = 1;

    private TabLayout tabLayout;
    private LinearLayout dashboardContainer;
    private LinearLayout moviesContainer;

    private EditText etMonth;
    private Button btnLoadReports;
    private ProgressBar dashboardProgressBar;

    private TextView tvTotalOrders;
    private TextView tvTotalRevenue;
    private TextView tvPromoDiscount;
    private TextView tvCouponDiscount;
    private TextView tvOrderTypeSummary;
    private TextView tvOrderStatusSummary;
    private TextView tvPromotionUsageSummary;
    private BarChart chartPromotionUsage;

    private RecyclerView rvMovies;
    private ProgressBar moviesProgressBar;
    private TextView tvMoviesEmpty;
    private FloatingActionButton fabAddMovie;
    private MovieAdapter movieAdapter;

    private SessionManager sessionManager;
    private MovieApiService apiService;
    private int selectedTab = TAB_DASHBOARD;
    private int dashboardRequestVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }
        apiService = ApiClient.getApiService();

        if (!isCurrentUserAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập dashboard", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupTabs();
        setupDefaultDates();
        showTab(TAB_DASHBOARD);
    }

    private boolean isCurrentUserAdmin() {
        User user = sessionManager.getUser();
        return user != null && user.isAdmin();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trung tâm quản trị");
        }

        tabLayout = findViewById(R.id.tab_layout);
        dashboardContainer = findViewById(R.id.layout_dashboard_container);
        moviesContainer = findViewById(R.id.layout_movies_container);

        etMonth = findViewById(R.id.et_month);
        btnLoadReports = findViewById(R.id.btn_load_reports);
        dashboardProgressBar = findViewById(R.id.progress_dashboard);

        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvPromoDiscount = findViewById(R.id.tv_promo_discount);
        tvCouponDiscount = findViewById(R.id.tv_coupon_discount);
        tvOrderTypeSummary = findViewById(R.id.tv_order_type_summary);
        tvOrderStatusSummary = findViewById(R.id.tv_order_status_summary);
        tvPromotionUsageSummary = findViewById(R.id.tv_promotion_usage_summary);
        chartPromotionUsage = findViewById(R.id.chart_promotion_usage);
        setupPromotionChart();

        rvMovies = findViewById(R.id.rv_movies);
        moviesProgressBar = findViewById(R.id.progress_movies);
        tvMoviesEmpty = findViewById(R.id.tv_movies_empty);
        fabAddMovie = findViewById(R.id.fab_add_movie);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(new ArrayList<>(), this::onMovieClick);
        rvMovies.setAdapter(movieAdapter);

        btnLoadReports.setOnClickListener(v -> loadDashboard());
        fabAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMovieActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Dashboard"), true);
        tabLayout.addTab(tabLayout.newTab().setText("Phim"));
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
                if (tab.getPosition() == TAB_DASHBOARD) {
                    loadDashboard();
                } else if (tab.getPosition() == TAB_MOVIES) {
                    loadAdminMovies();
                }
            }
        });
    }

    private void showTab(int tabIndex) {
        selectedTab = tabIndex;
        if (tabIndex == TAB_DASHBOARD) {
            dashboardContainer.setVisibility(View.VISIBLE);
            moviesContainer.setVisibility(View.GONE);
            fabAddMovie.hide();
            loadDashboard();
        } else {
            dashboardContainer.setVisibility(View.GONE);
            moviesContainer.setVisibility(View.VISIBLE);
            fabAddMovie.show();
            loadAdminMovies();
        }
    }

    private void setupDefaultDates() {
        String currentMonth = new SimpleDateFormat(MONTH_PATTERN, Locale.getDefault())
                .format(new Date());
        etMonth.setText(currentMonth);
    }

    private String formatDate(Calendar calendar) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(calendar.getTime());
    }

    private boolean validateMonthInput(String month) {
        if (TextUtils.isEmpty(month)) {
            Toast.makeText(this, "Vui lòng nhập tháng báo cáo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!month.matches("\\d{4}-\\d{2}")) {
            Toast.makeText(this, "Định dạng tháng phải là yyyy-MM", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String[] resolveDateRangeByMonth(String month) {
        try {
            SimpleDateFormat monthParser = new SimpleDateFormat(MONTH_PATTERN, Locale.getDefault());
            monthParser.setLenient(false);
            Date parsedMonth = monthParser.parse(month);
            if (parsedMonth == null) return null;

            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String startDate = formatDate(cal);

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = formatDate(cal);

            return new String[]{startDate, endDate};
        } catch (ParseException e) {
            return null;
        }
    }

    private void loadDashboard() {
        String month = etMonth.getText().toString().trim();
        if (!validateMonthInput(month)) return;

        String[] monthParts = month.split("-");
        if (monthParts.length != 2) {
            Toast.makeText(this, "Định dạng tháng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer year;
        Integer monthNumber;
        try {
            year = Integer.parseInt(monthParts[0]);
            monthNumber = Integer.parseInt(monthParts[1]);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Định dạng tháng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] dateRange = resolveDateRangeByMonth(month);
        if (dateRange == null) {
            Toast.makeText(this, "Không thể phân tích tháng đã nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        dashboardProgressBar.setVisibility(View.VISIBLE);
        btnLoadReports.setEnabled(false);
        pendingResponses = 0;
        dashboardRequestVersion++;
        int currentVersion = dashboardRequestVersion;

        AdminReportRequest request = new AdminReportRequest(monthNumber, year, startDate, endDate);

        loadRevenueReport(request, currentVersion);
        loadOrderVolumeReport(request, currentVersion);
        loadPromotionUsage(startDate, endDate, currentVersion);
    }

    private void loadRevenueReport(AdminReportRequest request, int requestVersion) {
        apiService.getAdminRevenueReport(request).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    bindRevenueReport(response.body().getResult());
                } else {
                    setRevenueFallback();
                }
                stopLoadingIfDone(requestVersion);
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                Log.e(TAG, "loadRevenueReport failed", t);
                setRevenueFallback();
                stopLoadingIfDone(requestVersion);
            }
        });
    }

    private void loadOrderVolumeReport(AdminReportRequest request, int requestVersion) {
        apiService.getAdminOrderVolumeOverview(request).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    bindOrderVolume(response.body().getResult());
                } else {
                    setOrderVolumeFallback();
                }
                stopLoadingIfDone(requestVersion);
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                Log.e(TAG, "loadOrderVolumeReport failed", t);
                setOrderVolumeFallback();
                stopLoadingIfDone(requestVersion);
            }
        });
    }

    private void loadPromotionUsage(String startDate, String endDate, int requestVersion) {
        apiService.getAdminPromotionUsage(startDate, endDate)
                .enqueue(new Callback<ApiResponse<JsonElement>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<JsonElement>> call,
                                           Response<ApiResponse<JsonElement>> response) {
                        if (!isActiveDashboardRequest(requestVersion)) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                            bindPromotionUsageChart(response.body().getResult());
                        } else {
                            showPromotionUsageEmpty("Không có dữ liệu sử dụng khuyến mãi");
                        }
                        stopLoadingIfDone(requestVersion);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                        if (!isActiveDashboardRequest(requestVersion)) return;
                        Log.e(TAG, "loadPromotionUsage failed", t);
                        showPromotionUsageEmpty("Không có dữ liệu sử dụng khuyến mãi");
                        stopLoadingIfDone(requestVersion);
                    }
                });
    }

    private int pendingResponses = 0;

    private void stopLoadingIfDone(int requestVersion) {
        if (!isActiveDashboardRequest(requestVersion)) return;
        pendingResponses++;
        if (pendingResponses >= 3) {
            dashboardProgressBar.setVisibility(View.GONE);
            btnLoadReports.setEnabled(true);
            pendingResponses = 0;
        }
    }

    private boolean isActiveDashboardRequest(int requestVersion) {
        return requestVersion == dashboardRequestVersion;
    }

    private void loadAdminMovies() {
        moviesProgressBar.setVisibility(View.VISIBLE);
        tvMoviesEmpty.setVisibility(View.GONE);

        apiService.getActiveMovies(0, 30).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call,
                                   Response<ApiResponse<PageResponse<Movie>>> response) {
                moviesProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<Movie> movies = response.body().getResult().getMovies();
                    if (movies == null || movies.isEmpty()) {
                        movieAdapter.updateData(new ArrayList<>());
                        tvMoviesEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvMoviesEmpty.setVisibility(View.GONE);
                        movieAdapter.updateData(movies);
                    }
                } else {
                    movieAdapter.updateData(new ArrayList<>());
                    tvMoviesEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                moviesProgressBar.setVisibility(View.GONE);
                movieAdapter.updateData(new ArrayList<>());
                tvMoviesEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi tải danh sách phim: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    private void bindRevenueReport(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            setRevenueFallback();
            return;
        }

        JsonObject root = element.getAsJsonObject();
        JsonObject obj = root;
        if (root.has("summary") && root.get("summary").isJsonObject()) {
            obj = root.getAsJsonObject("summary");
        }

        String totalRevenue = pickString(obj,
                "totalRevenue", "revenue", "grossRevenue", "totalAmount");
        String promoDiscount = pickString(obj,
                "promotionDiscount", "totalPromotionDiscount", "promotionDiscountAmount");
        String couponDiscount = pickString(obj,
                "couponDiscount", "totalCouponDiscount", "couponDiscountAmount");

        tvTotalRevenue.setText(formatCurrencyOrRaw(totalRevenue));
        tvPromoDiscount.setText(formatCurrencyOrRaw(promoDiscount));
        tvCouponDiscount.setText(formatCurrencyOrRaw(couponDiscount));

        String ticketSeat = pickString(root, "ticketSeat", "ticket", "ticketSeatCount");
        String food = pickString(root, "food", "foodCount", "foodOrderCount");

        if ((TextUtils.isEmpty(ticketSeat) && TextUtils.isEmpty(food))
                && root.has("details") && root.get("details").isJsonArray()) {
            int ticketSeatCount = 0;
            int foodCount = 0;
            for (JsonElement detailEl : root.getAsJsonArray("details")) {
                if (!detailEl.isJsonObject()) continue;
                JsonObject detail = detailEl.getAsJsonObject();
                String productName = pickString(detail, "productName", "name").toLowerCase(Locale.ROOT);
                String qty = pickString(detail, "quantitySold", "quantity", "sold");
                int quantity = 0;
                try {
                    quantity = TextUtils.isEmpty(qty) ? 0 : Integer.parseInt(qty);
                } catch (NumberFormatException ignored) {
                }
                if (productName.contains("popcorn") || productName.contains("combo")
                        || productName.contains("drink") || productName.contains("food")) {
                    foodCount += quantity;
                } else {
                    ticketSeatCount += quantity;
                }
            }
            ticketSeat = String.valueOf(ticketSeatCount);
            food = String.valueOf(foodCount);
        }

        if (!TextUtils.isEmpty(ticketSeat) || !TextUtils.isEmpty(food)) {
            tvOrderTypeSummary.setText("Vé/Ghế: " + formatCountOrZero(ticketSeat)
                    + "\nĐồ ăn: " + formatCountOrZero(food));
        }
    }

    private void bindOrderVolume(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            setOrderVolumeFallback();
            return;
        }

        JsonObject obj = element.getAsJsonObject();
        String total = pickString(obj, "total", "totalOrders", "orderCount");
        tvTotalOrders.setText(formatCountOrZero(total));

        JsonObject statusObj = obj.has("statusDistribution") && obj.get("statusDistribution").isJsonObject()
            ? obj.getAsJsonObject("statusDistribution") : obj;
        String fulfilled = pickString(statusObj,
            "fulfilled", "paid", "paidCount", "confirmed", "confirmedCount");
        String cancelled = pickString(statusObj,
            "cancelled", "canceled", "cancelledCount");
        tvOrderStatusSummary.setText("Hoàn tất: " + formatCountOrZero(fulfilled)
                + "\nĐã hủy: " + formatCountOrZero(cancelled));

        JsonObject categoryObj = obj.has("ordersByCategory") && obj.get("ordersByCategory").isJsonObject()
            ? obj.getAsJsonObject("ordersByCategory") : obj;
        String ticketSeat = pickString(categoryObj,
            "TicketSeat", "ticketSeat", "ticket", "ticketSeatCount");
        String food = pickString(categoryObj,
            "food", "Food", "foodCount", "foodOrderCount");
        if (!TextUtils.isEmpty(ticketSeat) || !TextUtils.isEmpty(food)) {
            tvOrderTypeSummary.setText("Vé/Ghế: " + formatCountOrZero(ticketSeat)
                    + "\nĐồ ăn: " + formatCountOrZero(food));
        }
    }

    private void setRevenueFallback() {
        tvTotalRevenue.setText("0 VNĐ");
        tvPromoDiscount.setText("0 VNĐ");
        tvCouponDiscount.setText("0 VNĐ");
        }

        private void setOrderVolumeFallback() {
        tvTotalOrders.setText("0");
        tvOrderTypeSummary.setText("Vé/Ghế: 0\nĐồ ăn: 0");
        tvOrderStatusSummary.setText("Hoàn tất: 0\nĐã hủy: 0");
    }

    private void setupPromotionChart() {
        chartPromotionUsage.setNoDataText("");
        chartPromotionUsage.getDescription().setEnabled(false);
        chartPromotionUsage.setScaleEnabled(false);
        chartPromotionUsage.setPinchZoom(false);
        chartPromotionUsage.setDrawGridBackground(false);

        XAxis xAxis = chartPromotionUsage.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-20f);

        YAxis leftAxis = chartPromotionUsage.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = chartPromotionUsage.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = chartPromotionUsage.getLegend();
        legend.setEnabled(false);
    }

    private void bindPromotionUsageChart(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            showPromotionUsageEmpty("Không có dữ liệu sử dụng khuyến mãi");
            return;
        }

        List<String> labels = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        int index = 0;

        for (JsonElement item : element.getAsJsonArray()) {
            if (!item.isJsonObject()) continue;

            JsonObject obj = item.getAsJsonObject();
            String code = pickString(obj, "promotionCode", "code", "couponCode", "name");
            String used = pickString(obj, "usedCount", "usageCount", "count", "timesUsed");

            float usage = parseUsageValue(used);
            labels.add(!TextUtils.isEmpty(code) ? code : "Mã " + (index + 1));
            entries.add(new BarEntry(index, usage));
            index++;
        }

        if (entries.isEmpty()) {
            showPromotionUsageEmpty("Không có dữ liệu sử dụng khuyến mãi");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Lượt sử dụng");
        dataSet.setColor(Color.parseColor("#2563EB"));
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.55f);

        chartPromotionUsage.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartPromotionUsage.getXAxis().setLabelCount(labels.size(), false);
        chartPromotionUsage.setData(data);
        chartPromotionUsage.setFitBars(true);
        chartPromotionUsage.invalidate();

        chartPromotionUsage.setVisibility(View.VISIBLE);
        tvPromotionUsageSummary.setVisibility(View.GONE);
    }

    private void showPromotionUsageEmpty(String message) {
        chartPromotionUsage.clear();
        chartPromotionUsage.setVisibility(View.GONE);
        tvPromotionUsageSummary.setVisibility(View.VISIBLE);
        tvPromotionUsageSummary.setText(message);
    }

    private float parseUsageValue(String value) {
        if (TextUtils.isEmpty(value)) return 0f;
        try {
            String normalized = value.replaceAll("[^0-9,.-]", "").replace(",", "");
            if (TextUtils.isEmpty(normalized) || "-".equals(normalized) || ".".equals(normalized)) {
                return 0f;
            }
            return (float) Math.max(0d, Double.parseDouble(normalized));
        } catch (Exception e) {
            return 0f;
        }
    }

    private String pickString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return valueToString(obj.get(key));
            }
        }
        return "";
    }

    private String valueToString(JsonElement value) {
        if (value == null || value.isJsonNull()) return "";
        if (value.isJsonPrimitive()) {
            return value.getAsJsonPrimitive().getAsString();
        }
        return value.toString();
    }

    private String formatCountOrZero(String value) {
        if (TextUtils.isEmpty(value)) return "0";
        try {
            String normalized = value.replaceAll("[^0-9,.-]", "").replace(",", "");
            if (TextUtils.isEmpty(normalized) || "-".equals(normalized) || ".".equals(normalized)) {
                return "0";
            }
            double parsed = Double.parseDouble(normalized);
            long rounded = Math.round(parsed);
            return NumberFormat.getIntegerInstance(VI_LOCALE).format(rounded);
        } catch (Exception e) {
            return value;
        }
    }

    private String formatCurrencyOrRaw(String value) {
        if (TextUtils.isEmpty(value)) return "0 VNĐ";
        try {
            double amount = Double.parseDouble(value.replace(",", ""));
            return CurrencyUtils.formatPrice(amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem adminDashboardItem = menu.findItem(R.id.action_admin_dashboard);
        MenuItem myBookingsItem = menu.findItem(R.id.action_my_bookings);
        if (adminDashboardItem != null) adminDashboardItem.setVisible(false);
        if (myBookingsItem != null) myBookingsItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            ApiClient.resetClient();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedTab == TAB_MOVIES) {
            loadAdminMovies();
        }
    }
}
