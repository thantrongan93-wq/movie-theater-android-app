package com.example.lab10.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.adapters.ShowtimeAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.AdminReportRequest;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.Showtime;
import com.example.lab10.models.User;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
    private static final int TAB_MANAGEMENT = 1;
    private static final int MANAGEMENT_SUBTAB_MOVIE = 0;
    private static final int MANAGEMENT_SUBTAB_SHOWTIME = 1;

    private TabLayout tabLayout;
    private LinearLayout dashboardContainer;
    private LinearLayout moviesContainer;
    private LinearLayout managementMovieContainer;
    private LinearLayout managementShowtimeContainer;
    private Button btnManagementMovie;
    private Button btnManagementShowtime;

    private EditText etMonth;
    private EditText etShowtimeDetailId;
    private Button btnLoadReports;
    private Button btnLoadShowtimeDetail;
    private Button btnLoadShowtime;
    private Button btnAddShowtime;
    private Button btnAddShowtimeDetail;
    private ProgressBar dashboardProgressBar;

    private TextView tvTotalOrders;
    private TextView tvTotalRevenue;
    private TextView tvPromoDiscount;
    private TextView tvCouponDiscount;
    private TextView tvOrderTypeSummary;
    private TextView tvOrderStatusSummary;
    private TextView tvPromotionUsageSummary;
    private PieChart chartOrderType;
    private PieChart chartOrderStatus;
    private BarChart chartPromotionUsage;

    private RecyclerView rvMovies;
    private RecyclerView rvShowtimeDetails;
    private ProgressBar moviesProgressBar;
    private ProgressBar showtimeProgressBar;
    private TextView tvMoviesEmpty;
    private TextView tvShowtimeEmpty;
    private TextView tvShowtimeSelectedDate;
    private CalendarView calendarShowtime;
    private FloatingActionButton fabAddMovie;
    private MovieAdapter movieAdapter;
    private ShowtimeAdapter showtimeAdapter;

    private SessionManager sessionManager;
    private MovieApiService apiService;
    private int selectedTab = TAB_DASHBOARD;
    private int selectedManagementSubTab = MANAGEMENT_SUBTAB_MOVIE;
    private int dashboardRequestVersion = 0;
    private JsonElement lastRevenueResult;
    private JsonElement lastOrderVolumeResult;
    private JsonElement lastPromotionUsageResult;

    private static class DashboardBatchResult {
        JsonElement revenueResult;
        JsonElement orderVolumeResult;
        JsonElement promotionUsageResult;
        boolean revenueSuccess;
        boolean orderVolumeSuccess;
        boolean promotionUsageSuccess;
    }

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
        managementMovieContainer = findViewById(R.id.layout_management_movie);
        managementShowtimeContainer = findViewById(R.id.layout_management_showtime);
        btnManagementMovie = findViewById(R.id.btn_management_movie);
        btnManagementShowtime = findViewById(R.id.btn_management_showtime);

        etMonth = findViewById(R.id.et_month);
        etShowtimeDetailId = findViewById(R.id.et_showtime_detail_id);
        btnLoadReports = findViewById(R.id.btn_load_reports);
        btnLoadShowtimeDetail = findViewById(R.id.btn_load_showtime_detail);
        btnLoadShowtime = findViewById(R.id.btn_load_showtime);
        btnAddShowtime = findViewById(R.id.btn_add_showtime);
        btnAddShowtimeDetail = findViewById(R.id.btn_add_showtime_detail);
        dashboardProgressBar = findViewById(R.id.progress_dashboard);

        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvPromoDiscount = findViewById(R.id.tv_promo_discount);
        tvCouponDiscount = findViewById(R.id.tv_coupon_discount);
        tvOrderTypeSummary = findViewById(R.id.tv_order_type_summary);
        tvOrderStatusSummary = findViewById(R.id.tv_order_status_summary);
        tvPromotionUsageSummary = findViewById(R.id.tv_promotion_usage_summary);
        chartOrderType = findViewById(R.id.chart_order_type);
        chartOrderStatus = findViewById(R.id.chart_order_status);
        chartPromotionUsage = findViewById(R.id.chart_promotion_usage);
        setupOrderPieCharts();
        setupPromotionChart();

        rvMovies = findViewById(R.id.rv_movies);
        rvShowtimeDetails = findViewById(R.id.rv_showtime_details);
        moviesProgressBar = findViewById(R.id.progress_movies);
        showtimeProgressBar = findViewById(R.id.progress_showtime_details);
        tvMoviesEmpty = findViewById(R.id.tv_movies_empty);
        tvShowtimeEmpty = findViewById(R.id.tv_showtime_empty);
        tvShowtimeSelectedDate = findViewById(R.id.tv_showtime_selected_date);
        calendarShowtime = findViewById(R.id.calendar_showtime);
        fabAddMovie = findViewById(R.id.fab_add_movie);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(
            new ArrayList<>(),
            this::onMovieClick,
            R.layout.item_movie_admin_simple,
            true
        );
        rvMovies.setAdapter(movieAdapter);

        rvShowtimeDetails.setLayoutManager(new LinearLayoutManager(this));
        showtimeAdapter = new ShowtimeAdapter(new ArrayList<>(), this::onManagementShowtimeClick, false);
        rvShowtimeDetails.setAdapter(showtimeAdapter);

        btnLoadReports.setOnClickListener(v -> loadDashboard());
        btnManagementMovie.setOnClickListener(v -> showManagementSubTab(MANAGEMENT_SUBTAB_MOVIE));
        btnLoadShowtime.setOnClickListener(v -> loadShowtime());
        btnManagementShowtime.setOnClickListener(v -> showManagementSubTab(MANAGEMENT_SUBTAB_SHOWTIME));
        btnLoadShowtimeDetail.setOnClickListener(v -> loadShowtimeDetail());
        btnAddShowtime.setOnClickListener(v -> showCreateShowtimeDialog());
        btnAddShowtimeDetail.setOnClickListener(v -> showCreateShowtimeDetailDialog());
        fabAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMovieActivity.class);
            startActivity(intent);
        });

        if (calendarShowtime != null) {
            calendarShowtime.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                String apiDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                String displayDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                loadShowtimeDetailByDate(apiDate, displayDate);
            });
        }
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Dashboard"), true);
        tabLayout.addTab(tabLayout.newTab().setText("Management"));
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
                } else if (tab.getPosition() == TAB_MANAGEMENT) {
                    reloadManagementSubTab();
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
            showManagementSubTab(selectedManagementSubTab);
        }
    }

    private void showManagementSubTab(int subTabIndex) {
        selectedManagementSubTab = subTabIndex;
        boolean isMovieSubTab = subTabIndex == MANAGEMENT_SUBTAB_MOVIE;

        managementMovieContainer.setVisibility(isMovieSubTab ? View.VISIBLE : View.GONE);
        managementShowtimeContainer.setVisibility(isMovieSubTab ? View.GONE : View.VISIBLE);
        fabAddMovie.setVisibility(isMovieSubTab ? View.VISIBLE : View.GONE);

        btnManagementMovie.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(
                isMovieSubTab ? "#2563EB" : "#E5E7EB")));
        btnManagementMovie.setTextColor(Color.parseColor(isMovieSubTab ? "#FFFFFF" : "#111827"));

        btnManagementShowtime.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(
                isMovieSubTab ? "#E5E7EB" : "#2563EB")));
        btnManagementShowtime.setTextColor(Color.parseColor(isMovieSubTab ? "#111827" : "#FFFFFF"));

        if (isMovieSubTab) {
            loadAdminMovies();
        } else {
            loadShowtimeByCalendarSelection();
        }
    }

    private void reloadManagementSubTab() {
        if (selectedManagementSubTab == MANAGEMENT_SUBTAB_MOVIE) {
            loadAdminMovies();
        } else {
            loadShowtimeByCalendarSelection();
        }
    }

    private void loadShowtimeByCalendarSelection() {
        if (calendarShowtime == null) {
            loadShowtime();
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(calendarShowtime.getDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String apiDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
        String displayDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year);
        loadShowtimeDetailByDate(apiDate, displayDate);
    }

    private void loadShowtime() {
        showtimeProgressBar.setVisibility(View.VISIBLE);
        tvShowtimeEmpty.setVisibility(View.GONE);

        apiService.getAllShowtimes().enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call,
                                   Response<ApiResponse<List<Showtime>>> response) {
                showtimeProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<Showtime> showtimes = response.body().getResult();
                    if (showtimes == null || showtimes.isEmpty()) {
                        showtimeAdapter.updateData(new ArrayList<>());
                        tvShowtimeEmpty.setVisibility(View.VISIBLE);
                        tvShowtimeEmpty.setText("Không có showtime");
                    } else {
                        showtimeAdapter.updateData(showtimes);
                        tvShowtimeEmpty.setVisibility(View.GONE);
                    }
                } else {
                    showtimeAdapter.updateData(new ArrayList<>());
                    tvShowtimeEmpty.setVisibility(View.VISIBLE);
                    tvShowtimeEmpty.setText("Không tải được danh sách showtime");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                showtimeProgressBar.setVisibility(View.GONE);
                showtimeAdapter.updateData(new ArrayList<>());
                tvShowtimeEmpty.setVisibility(View.VISIBLE);
                tvShowtimeEmpty.setText("Không có showtime");
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi tải showtime: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        DashboardBatchResult batchResult = new DashboardBatchResult();

        loadRevenueReport(request, currentVersion, batchResult);
        loadOrderVolumeReport(request, currentVersion, batchResult);
        loadPromotionUsage(startDate, endDate, currentVersion, batchResult);
    }

    private void loadRevenueReport(AdminReportRequest request, int requestVersion,
                                   DashboardBatchResult batchResult) {
        apiService.getAdminRevenueReport(request).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    batchResult.revenueSuccess = true;
                    batchResult.revenueResult = response.body().getResult();
                } else {
                    batchResult.revenueSuccess = false;
                }
                stopLoadingIfDone(requestVersion, batchResult);
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                Log.e(TAG, "loadRevenueReport failed", t);
                batchResult.revenueSuccess = false;
                stopLoadingIfDone(requestVersion, batchResult);
            }
        });
    }

    private void loadOrderVolumeReport(AdminReportRequest request, int requestVersion,
                                       DashboardBatchResult batchResult) {
        apiService.getAdminOrderVolumeOverview(request).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    batchResult.orderVolumeSuccess = true;
                    batchResult.orderVolumeResult = response.body().getResult();
                } else {
                    batchResult.orderVolumeSuccess = false;
                }
                stopLoadingIfDone(requestVersion, batchResult);
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                if (!isActiveDashboardRequest(requestVersion)) return;
                Log.e(TAG, "loadOrderVolumeReport failed", t);
                batchResult.orderVolumeSuccess = false;
                stopLoadingIfDone(requestVersion, batchResult);
            }
        });
    }

    private void loadPromotionUsage(String startDate, String endDate, int requestVersion,
                                    DashboardBatchResult batchResult) {
        apiService.getAdminPromotionUsage(startDate, endDate)
                .enqueue(new Callback<ApiResponse<JsonElement>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<JsonElement>> call,
                                           Response<ApiResponse<JsonElement>> response) {
                        if (!isActiveDashboardRequest(requestVersion)) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                            batchResult.promotionUsageSuccess = true;
                            batchResult.promotionUsageResult = response.body().getResult();
                        } else {
                            batchResult.promotionUsageSuccess = false;
                        }
                        stopLoadingIfDone(requestVersion, batchResult);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                        if (!isActiveDashboardRequest(requestVersion)) return;
                        Log.e(TAG, "loadPromotionUsage failed", t);
                        batchResult.promotionUsageSuccess = false;
                        stopLoadingIfDone(requestVersion, batchResult);
                    }
                });
    }

    private int pendingResponses = 0;

    private void stopLoadingIfDone(int requestVersion, DashboardBatchResult batchResult) {
        if (!isActiveDashboardRequest(requestVersion)) return;
        pendingResponses++;
        if (pendingResponses >= 3) {
            applyDashboardBatchResult(batchResult);
            dashboardProgressBar.setVisibility(View.GONE);
            btnLoadReports.setEnabled(true);
            pendingResponses = 0;
        }
    }

    private void applyDashboardBatchResult(DashboardBatchResult batchResult) {
        if (batchResult.revenueSuccess && batchResult.revenueResult != null) {
            lastRevenueResult = batchResult.revenueResult;
            bindRevenueReport(batchResult.revenueResult);
        } else if (lastRevenueResult != null) {
            bindRevenueReport(lastRevenueResult);
        } else {
            setRevenueFallback();
        }

        if (batchResult.orderVolumeSuccess && batchResult.orderVolumeResult != null) {
            lastOrderVolumeResult = batchResult.orderVolumeResult;
            bindOrderVolume(batchResult.orderVolumeResult);
        } else if (lastOrderVolumeResult != null) {
            bindOrderVolume(lastOrderVolumeResult);
        } else {
            setOrderVolumeFallback();
        }

        if (batchResult.promotionUsageSuccess && batchResult.promotionUsageResult != null) {
            lastPromotionUsageResult = batchResult.promotionUsageResult;
            bindPromotionUsageChart(batchResult.promotionUsageResult);
        } else if (lastPromotionUsageResult != null) {
            bindPromotionUsageChart(lastPromotionUsageResult);
        } else {
            showPromotionUsageEmpty("Không có dữ liệu sử dụng khuyến mãi");
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

    private void onManagementShowtimeClick(Showtime showtime) {
        if (showtime == null || showtime.getId() == null) return;
        etShowtimeDetailId.setText(String.valueOf(showtime.getId()));
        Toast.makeText(this, "Đã chọn suất chiếu #" + showtime.getId(), Toast.LENGTH_SHORT).show();
    }

    private void loadShowtimeDetail() {
        String showtimeDetailIdText = etShowtimeDetailId.getText().toString().trim();
        Long showtimeDetailId = null;
        if (!TextUtils.isEmpty(showtimeDetailIdText)) {
            try {
                showtimeDetailId = Long.parseLong(showtimeDetailIdText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "showtimeDetailId không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showtimeProgressBar.setVisibility(View.VISIBLE);
        tvShowtimeEmpty.setVisibility(View.GONE);

        Long filterShowtimeDetailId = showtimeDetailId;
        apiService.getAllShowtimeDetails().enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call,
                                   Response<ApiResponse<List<Showtime>>> response) {
                showtimeProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Showtime> allDetails = response.body().getResult();
                    List<Showtime> details = new ArrayList<>();
                    if (allDetails != null) {
                        for (Showtime detail : allDetails) {
                            if (detail == null) continue;
                            if (TextUtils.isEmpty(detail.getShowDate())
                                    && !TextUtils.isEmpty(detail.getStartTime())
                                    && detail.getStartTime().contains("T")) {
                                detail.setShowDate(detail.getStartTime().split("T")[0]);
                            }
                            if (filterShowtimeDetailId != null
                                    && !filterShowtimeDetailId.equals(detail.getId())) {
                                continue;
                            }
                            details.add(detail);
                        }
                    }

                    if (details.isEmpty()) {
                        showtimeAdapter.updateData(new ArrayList<>());
                        tvShowtimeEmpty.setVisibility(View.VISIBLE);
                        tvShowtimeEmpty.setText(filterShowtimeDetailId == null
                                ? "Không có suất chiếu"
                                : "Không tìm thấy suất chiếu #" + filterShowtimeDetailId);
                        return;
                    }

                    showtimeAdapter.updateData(details);
                    tvShowtimeEmpty.setVisibility(View.GONE);
                } else {
                    showtimeAdapter.updateData(new ArrayList<>());
                    tvShowtimeEmpty.setVisibility(View.VISIBLE);
                    tvShowtimeEmpty.setText("Không tải được danh sách suất chiếu");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                showtimeProgressBar.setVisibility(View.GONE);
                showtimeAdapter.updateData(new ArrayList<>());
                tvShowtimeEmpty.setVisibility(View.VISIBLE);
                tvShowtimeEmpty.setText("Không có suất chiếu");
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi tải danh sách suất chiếu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShowtimeDetailByDate(String apiDate, String displayDate) {
        showtimeProgressBar.setVisibility(View.VISIBLE);
        tvShowtimeEmpty.setVisibility(View.GONE);
        if (tvShowtimeSelectedDate != null) {
            tvShowtimeSelectedDate.setText("Danh sách suất chiếu ngày " + displayDate);
        }

        apiService.getShowtimeDetailsByDate(apiDate).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call,
                                   Response<ApiResponse<List<Showtime>>> response) {
                showtimeProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Showtime> details = response.body().getResult();
                    if (details == null || details.isEmpty()) {
                        showtimeAdapter.updateData(new ArrayList<>());
                        tvShowtimeEmpty.setVisibility(View.VISIBLE);
                        tvShowtimeEmpty.setText("Không có suất chiếu ngày " + displayDate);
                        return;
                    }

                    for (Showtime detail : details) {
                        if (detail == null) continue;
                        if (TextUtils.isEmpty(detail.getShowDate())
                                && !TextUtils.isEmpty(detail.getStartTime())
                                && detail.getStartTime().contains("T")) {
                            detail.setShowDate(detail.getStartTime().split("T")[0]);
                        }
                    }
                    showtimeAdapter.updateData(details);
                    tvShowtimeEmpty.setVisibility(View.GONE);
                } else {
                    showtimeAdapter.updateData(new ArrayList<>());
                    tvShowtimeEmpty.setVisibility(View.VISIBLE);
                    tvShowtimeEmpty.setText("Không tải được suất chiếu theo ngày");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                showtimeProgressBar.setVisibility(View.GONE);
                showtimeAdapter.updateData(new ArrayList<>());
                tvShowtimeEmpty.setVisibility(View.VISIBLE);
                tvShowtimeEmpty.setText("Không tải được suất chiếu theo ngày");
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi tải suất chiếu theo ngày: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateShowtimeDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etMovieId = new EditText(this);
        etMovieId.setHint("movieId");
        etMovieId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        container.addView(etMovieId);

        EditText etCinemaRoomId = new EditText(this);
        etCinemaRoomId.setHint("cinemaRoomId");
        etCinemaRoomId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        container.addView(etCinemaRoomId);

        new AlertDialog.Builder(this)
                .setTitle("Add Showtime")
                .setView(container)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String movieIdText = etMovieId.getText().toString().trim();
                    String roomIdText = etCinemaRoomId.getText().toString().trim();

                    if (TextUtils.isEmpty(movieIdText) || TextUtils.isEmpty(roomIdText)) {
                        Toast.makeText(this, "Vui lòng nhập movieId và cinemaRoomId", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long movieId;
                    Long cinemaRoomId;
                    try {
                        movieId = Long.parseLong(movieIdText);
                        cinemaRoomId = Long.parseLong(roomIdText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "movieId hoặc cinemaRoomId không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JsonObject payload = new JsonObject();
                    payload.addProperty("movieId", movieId);
                    payload.addProperty("cinemaRoomId", cinemaRoomId);

                    apiService.createShowtimeFromManagement(payload).enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call,
                                               Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "Tạo showtime thành công", Toast.LENGTH_SHORT).show();
                                loadShowtimeDetail();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "Tạo showtime thất bại (" + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Lỗi tạo showtime: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreateShowtimeDetailDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etShowtimeId = new EditText(this);
        etShowtimeId.setHint("showtimeId (path id)");
        etShowtimeId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        container.addView(etShowtimeId);

        EditText etStartTime = new EditText(this);
        etStartTime.setHint("startTime (vd: 2026-03-22T17:17:26.845Z)");
        etStartTime.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        container.addView(etStartTime);

        EditText etBasePrice = new EditText(this);
        etBasePrice.setHint("basePrice");
        etBasePrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        container.addView(etBasePrice);

        new AlertDialog.Builder(this)
                .setTitle("Add Showtime Detail")
                .setView(container)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String showtimeIdText = etShowtimeId.getText().toString().trim();
                    String startTime = etStartTime.getText().toString().trim();
                    String basePriceText = etBasePrice.getText().toString().trim();

                    if (TextUtils.isEmpty(showtimeIdText)
                            || TextUtils.isEmpty(startTime)
                            || TextUtils.isEmpty(basePriceText)) {
                        Toast.makeText(this,
                                "Vui lòng nhập showtimeId, startTime và basePrice",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long showtimeId;
                    Double basePrice;
                    try {
                        showtimeId = Long.parseLong(showtimeIdText);
                        basePrice = Double.parseDouble(basePriceText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "showtimeId hoặc basePrice không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JsonObject payload = new JsonObject();
                    payload.addProperty("startTime", startTime);
                    payload.addProperty("basePrice", basePrice);

                    apiService.createShowtimeDetail(showtimeId, payload)
                            .enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call,
                                               Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "Tạo showtime detail thành công", Toast.LENGTH_SHORT).show();
                                etShowtimeDetailId.setText("");
                                loadShowtimeDetail();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "Tạo showtime detail thất bại (" + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Lỗi tạo showtime detail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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
        bindOrderStatusPie(fulfilled, cancelled);

        JsonObject categoryObj = obj.has("ordersByCategory") && obj.get("ordersByCategory").isJsonObject()
            ? obj.getAsJsonObject("ordersByCategory") : obj;
        String ticketSeat = pickString(categoryObj,
            "TicketSeat", "ticketSeat", "ticket", "ticketSeatCount");
        String food = pickString(categoryObj,
            "food", "Food", "foodCount", "foodOrderCount");
        if (!TextUtils.isEmpty(ticketSeat) || !TextUtils.isEmpty(food)) {
            bindOrderTypePie(ticketSeat, food);
        }
    }

    private void setRevenueFallback() {
        tvTotalRevenue.setText("0 VNĐ");
        tvPromoDiscount.setText("0 VNĐ");
        tvCouponDiscount.setText("0 VNĐ");
        }

        private void setOrderVolumeFallback() {
        tvTotalOrders.setText("0");
        showOrderTypeEmpty("Chưa có dữ liệu phân loại đơn");
        showOrderStatusEmpty("Chưa có dữ liệu trạng thái đơn hàng");
    }

    private void setupOrderPieCharts() {
        setupPieChart(chartOrderType);
        setupPieChart(chartOrderStatus);
    }

    private void setupPieChart(PieChart chart) {
        if (chart == null) return;
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(58f);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        chart.setNoDataText("");

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(13f);
    }

    private void bindOrderTypePie(String ticketSeatValue, String foodValue) {
        float ticketSeat = parseUsageValue(ticketSeatValue);
        float food = parseUsageValue(foodValue);

        if (ticketSeat <= 0f && food <= 0f) {
            showOrderTypeEmpty("Chưa có dữ liệu phân loại đơn");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        if (ticketSeat > 0f) entries.add(new PieEntry(ticketSeat, "TicketSeat"));
        if (food > 0f) entries.add(new PieEntry(food, "Food"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#1E88E5"),
                Color.parseColor("#14B8A6")
        );
        dataSet.setValueTextColor(Color.parseColor("#111827"));
        dataSet.setValueTextSize(16f);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);

        chartOrderType.setData(data);
        chartOrderType.invalidate();
        chartOrderType.setVisibility(View.VISIBLE);
        tvOrderTypeSummary.setVisibility(View.GONE);
    }

    private void bindOrderStatusPie(String fulfilledValue, String cancelledValue) {
        float fulfilled = parseUsageValue(fulfilledValue);
        float cancelled = parseUsageValue(cancelledValue);

        if (fulfilled <= 0f && cancelled <= 0f) {
            showOrderStatusEmpty("Chưa có dữ liệu trạng thái đơn hàng");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        if (fulfilled > 0f) entries.add(new PieEntry(fulfilled, "Fulfilled"));
        if (cancelled > 0f) entries.add(new PieEntry(cancelled, "Cancelled"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#1E88E5"),
                Color.parseColor("#14B8A6")
        );
        dataSet.setValueTextColor(Color.parseColor("#111827"));
        dataSet.setValueTextSize(16f);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);

        chartOrderStatus.setData(data);
        chartOrderStatus.invalidate();
        chartOrderStatus.setVisibility(View.VISIBLE);
        tvOrderStatusSummary.setVisibility(View.GONE);
    }

    private void showOrderTypeEmpty(String message) {
        if (chartOrderType != null) {
            chartOrderType.clear();
            chartOrderType.setVisibility(View.GONE);
        }
        tvOrderTypeSummary.setVisibility(View.VISIBLE);
        tvOrderTypeSummary.setText(message);
    }

    private void showOrderStatusEmpty(String message) {
        if (chartOrderStatus != null) {
            chartOrderStatus.clear();
            chartOrderStatus.setVisibility(View.GONE);
        }
        tvOrderStatusSummary.setVisibility(View.VISIBLE);
        tvOrderStatusSummary.setText(message);
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
        if (selectedTab == TAB_MANAGEMENT) {
            reloadManagementSubTab();
        }
    }
}
