package com.example.lab10.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.adapters.BookingShowtimeAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.ScanResponse;
import com.example.lab10.models.Showtime;
import com.example.lab10.models.ShowtimeGroup;
import com.example.lab10.models.User;
import com.example.lab10.utils.ImageLoader;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeDashboard";
    private static final int TAB_SCAN = 0;
    private static final int TAB_ORDER = 1;
    private static final int TOTAL_BOOKING_STEPS = 5;
    private static final int[] UPCOMING_PAGE_SIZES = {50, 20, 10};
    private static final int MAX_SHOWTIMES_RETRY = 2;

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
    private View layoutTotalPriceRow;
    private View dividerTotalPrice;
    private TextView tvCheckInStatus;
    private Button btnConfirmCheckIn;
    private Button btnNewScan;

    // Booking Flow views (Integrated)
    private int currentBookingStep = 0;
    private TextView[] stepIndicators;
    private LinearLayout[] stepContainers;
    private Button btnPreviousBookingStep;
    private Button btnNextBookingStep;
    private Button btnRestartBookingFlow;

    // Step 1: Movie selection
    private RecyclerView rvBookingMovies;
    private ProgressBar progressBookingMovies;
    private MovieAdapter bookingMovieAdapter;
    private Movie selectedMovie;

    // Step 2: Showtime selection
    private ImageView ivMoviePoster;
    private TextView tvMovieTitle, tvMovieInfoDetail, tvMovieGenre, tvMovieDirector;
    private RecyclerView rvBookingShowtimes;
    private ProgressBar progressBookingShowtimes;
    private TextView tvShowtimesEmpty;
    private BookingShowtimeAdapter bookingShowtimeAdapter; 

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
        layoutTotalPriceRow = findViewById(R.id.layout_total_price_row);
        dividerTotalPrice = findViewById(R.id.divider_total_price);
        tvCheckInStatus = findViewById(R.id.tv_checkin_status);
        btnConfirmCheckIn = findViewById(R.id.btn_confirm_checkin);
        btnNewScan = findViewById(R.id.btn_new_scan);

        btnScan.setOnClickListener(v -> startCameraQrScan());
        btnConfirmCheckIn.setOnClickListener(v -> confirmCheckIn());
        btnNewScan.setOnClickListener(v -> resetScanForm());

        // Booking Flow Views
        stepIndicators = new TextView[] {
                findViewById(R.id.step_movie),
                findViewById(R.id.step_time),
                findViewById(R.id.step_seat),
                findViewById(R.id.step_confirm),
                findViewById(R.id.step_done)
        };

        stepContainers = new LinearLayout[] {
                findViewById(R.id.layout_step_movie),
                findViewById(R.id.layout_step_time),
                findViewById(R.id.layout_step_seat),
                findViewById(R.id.layout_step_confirm),
                findViewById(R.id.layout_step_done)
        };

        btnPreviousBookingStep = findViewById(R.id.btn_previous_step);
        btnNextBookingStep = findViewById(R.id.btn_next_step);
        btnRestartBookingFlow = findViewById(R.id.btn_restart_flow);

        // Step 1 Setup
        rvBookingMovies = findViewById(R.id.rv_booking_movies);
        progressBookingMovies = findViewById(R.id.progress_booking_movies);
        rvBookingMovies.setLayoutManager(new GridLayoutManager(this, 2));
        bookingMovieAdapter = new MovieAdapter(new ArrayList<>(), movie -> {
            this.selectedMovie = movie;
            onMovieSelectedForBooking(movie.getMovieId());
        });
        rvBookingMovies.setAdapter(bookingMovieAdapter);

        // Step 2 Views Setup
        ivMoviePoster = findViewById(R.id.iv_booking_movie_poster);
        tvMovieTitle = findViewById(R.id.tv_booking_movie_title);
        tvMovieInfoDetail = findViewById(R.id.tv_booking_movie_info);
        tvMovieGenre = findViewById(R.id.tv_booking_movie_genre);
        tvMovieDirector = findViewById(R.id.tv_booking_movie_director);
        rvBookingShowtimes = findViewById(R.id.rv_booking_showtimes);
        progressBookingShowtimes = findViewById(R.id.progress_booking_showtimes);
        tvShowtimesEmpty = findViewById(R.id.tv_booking_showtimes_empty);

        rvBookingShowtimes.setLayoutManager(new LinearLayoutManager(this));
        bookingShowtimeAdapter = new BookingShowtimeAdapter(new ArrayList<>(), showtimeId -> {
            bookingShowtimeAdapter.setSelectedShowtimeId(showtimeId);
            openSeatSelectionForEmployee(showtimeId);
        });
        rvBookingShowtimes.setAdapter(bookingShowtimeAdapter);

        btnPreviousBookingStep.setOnClickListener(v -> {
            if (currentBookingStep > 0) {
                currentBookingStep--;
                renderBookingStep();
            }
        });

        btnNextBookingStep.setOnClickListener(v -> {
            if (currentBookingStep < TOTAL_BOOKING_STEPS - 1) {
                currentBookingStep++;
                renderBookingStep();
            } else {
                tabLayout.getTabAt(TAB_SCAN).select();
            }
        });

        btnRestartBookingFlow.setOnClickListener(v -> {
            currentBookingStep = 0;
            renderBookingStep();
            loadMoviesForBooking();
        });
    }

    private void onMovieSelectedForBooking(Long movieId) {
        currentBookingStep = 1;
        renderBookingStep();
        loadMovieDetailAndShowtimes(movieId);
    }

    private void loadMovieDetailAndShowtimes(Long movieId) {
        progressBookingShowtimes.setVisibility(View.VISIBLE);
        tvShowtimesEmpty.setVisibility(View.GONE);

        apiService.getMovieById(movieId).enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body().getResult();
                    displayBookingMovieDetail(movie);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {}
        });

        loadShowtimesWithRetry(movieId, 0);
    }

    private void loadShowtimesWithRetry(Long movieId, int retryCount) {
        apiService.getMovieShowtimes(movieId).enqueue(new Callback<ApiResponse<List<ShowtimeGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShowtimeGroup>>> call, Response<ApiResponse<List<ShowtimeGroup>>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressBookingShowtimes.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ShowtimeGroup> groups = response.body().getResult();
                    if (groups == null || groups.isEmpty()) {
                        tvShowtimesEmpty.setVisibility(View.VISIBLE);
                        bookingShowtimeAdapter.updateGroupData(new ArrayList<>());
                    } else {
                        bookingShowtimeAdapter.updateGroupData(groups);
                    }
                } else {
                    tvShowtimesEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ShowtimeGroup>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (t instanceof EOFException && retryCount < MAX_SHOWTIMES_RETRY) {
                    Log.w(TAG, "getMovieShowtimes EOF, retry " + (retryCount + 1), t);
                    loadShowtimesWithRetry(movieId, retryCount + 1);
                    return;
                }

                progressBookingShowtimes.setVisibility(View.GONE);
                tvShowtimesEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "getMovieShowtimes failed", t);
                loadShowtimesFallback(movieId);
            }
        });
    }

    private void loadShowtimesFallback(Long movieId) {
        apiService.getShowtimesByMovie(movieId).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<ShowtimeGroup> grouped = groupShowtimes(response.body().getResult());
                    if (grouped.isEmpty()) {
                        tvShowtimesEmpty.setVisibility(View.VISIBLE);
                        bookingShowtimeAdapter.updateGroupData(new ArrayList<>());
                    } else {
                        tvShowtimesEmpty.setVisibility(View.GONE);
                        bookingShowtimeAdapter.updateGroupData(grouped);
                    }
                } else {
                    tvShowtimesEmpty.setVisibility(View.VISIBLE);
                    bookingShowtimeAdapter.updateGroupData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tvShowtimesEmpty.setVisibility(View.VISIBLE);
                bookingShowtimeAdapter.updateGroupData(new ArrayList<>());
                Toast.makeText(EmployeeDashboardActivity.this, "Không thể tải suất chiếu, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "loadShowtimesFallback failed", t);
            }
        });
    }

    private List<ShowtimeGroup> groupShowtimes(List<Showtime> showtimes) {
        Map<String, List<ShowtimeGroup.ShowtimeInfo>> grouped = new LinkedHashMap<>();
        if (showtimes == null) {
            return new ArrayList<>();
        }

        for (Showtime showtime : showtimes) {
            if (showtime == null || showtime.getId() == null) {
                continue;
            }
            String dateKey = !TextUtils.isEmpty(showtime.getShowDate()) ? showtime.getShowDate() : "Unknown";
            String timeValue = !TextUtils.isEmpty(showtime.getStartTime())
                    ? showtime.getStartTime()
                    : showtime.getShowTime();

            ShowtimeGroup.ShowtimeInfo info = new ShowtimeGroup.ShowtimeInfo();
            info.setShowtimeId(showtime.getId());
            info.setTime(timeValue != null ? timeValue : "00:00");

            List<ShowtimeGroup.ShowtimeInfo> list = grouped.get(dateKey);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(dateKey, list);
            }
            list.add(info);
        }

        List<ShowtimeGroup> result = new ArrayList<>();
        for (Map.Entry<String, List<ShowtimeGroup.ShowtimeInfo>> entry : grouped.entrySet()) {
            ShowtimeGroup group = new ShowtimeGroup();
            group.setDate(entry.getKey());
            group.setShowtimes(entry.getValue());
            result.add(group);
        }
        return result;
    }

    private void displayBookingMovieDetail(Movie movie) {
        if (movie == null) return;
        tvMovieTitle.setText(movie.getTitle());
        String info = (movie.getDuration() != null ? movie.getDuration() + " phút" : "") 
                      + (movie.getLanguage() != null ? " • " + movie.getLanguage() : "");
        tvMovieInfoDetail.setText(info);
        tvMovieGenre.setText(movie.getGenre());
        tvMovieDirector.setText("Đạo diễn: " + (movie.getDirector() != null ? movie.getDirector() : "N/A"));
        
        ImageLoader.loadImageWithPlaceholder(ivMoviePoster, movie.getPosterUrl(), R.drawable.ic_launcher_foreground);
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
                    currentBookingStep = 0;
                    renderBookingStep();
                    loadMoviesForBooking();
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
            currentBookingStep = 0;
            renderBookingStep();
            loadMoviesForBooking();
        }
    }

    private void loadMoviesForBooking() {
        loadMoviesForBooking(0);
    }

    private void loadMoviesForBooking(int retryIndex) {
        progressBookingMovies.setVisibility(View.VISIBLE);
        int size = UPCOMING_PAGE_SIZES[Math.min(retryIndex, UPCOMING_PAGE_SIZES.length - 1)];
        apiService.getUpcomingMovies(0, size).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressBookingMovies.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<Movie> movies = response.body().getResult().getMovies();
                    bookingMovieAdapter.updateData(movies != null ? movies : new ArrayList<>());
                } else {
                    Toast.makeText(EmployeeDashboardActivity.this, "Không thể tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (t instanceof EOFException && retryIndex < UPCOMING_PAGE_SIZES.length - 1) {
                    Log.w(TAG, "loadMoviesForBooking EOF, retry with smaller page size", t);
                    loadMoviesForBooking(retryIndex + 1);
                    return;
                }
                progressBookingMovies.setVisibility(View.GONE);
                Log.e(TAG, "loadMoviesForBooking failed", t);
                Toast.makeText(EmployeeDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openSeatSelectionForEmployee(Long showtimeDetailId) {
        if (showtimeDetailId == null) {
            Toast.makeText(this, "Không tìm thấy suất chiếu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedMovie == null) {
            Toast.makeText(this, "Vui lòng chọn phim trước", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBookingShowtimes.setVisibility(View.VISIBLE);
        apiService.getShowtimeDetailById(showtimeDetailId).enqueue(new Callback<ApiResponse<Showtime>>() {
            @Override
            public void onResponse(Call<ApiResponse<Showtime>> call, Response<ApiResponse<Showtime>> response) {
                progressBookingShowtimes.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    Showtime showtime = response.body().getResult();
                    if (showtime.getMovie() == null) {
                        showtime.setMovie(selectedMovie);
                    }
                    if (showtime.getMovieId() == null) {
                        showtime.setMovieId(selectedMovie.getMovieId());
                    }

                    Intent intent = new Intent(EmployeeDashboardActivity.this, SeatSelectionActivity.class);
                    intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
                    intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE, selectedMovie);
                    startActivity(intent);
                } else {
                    Toast.makeText(EmployeeDashboardActivity.this, "Không tải được thông tin suất chiếu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Showtime>> call, Throwable t) {
                progressBookingShowtimes.setVisibility(View.GONE);
                Log.e(TAG, "openSeatSelectionForEmployee failed", t);
                Toast.makeText(EmployeeDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderBookingStep() {
        for (int i = 0; i < stepContainers.length; i++) {
            stepContainers[i].setVisibility(i == currentBookingStep ? View.VISIBLE : View.GONE);
        }

        for (int i = 0; i < stepIndicators.length; i++) {
            if (i < currentBookingStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_done, android.R.color.white);
            } else if (i == currentBookingStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_active, android.R.color.white);
            } else {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_idle, android.R.color.darker_gray);
            }
        }

        btnPreviousBookingStep.setEnabled(currentBookingStep > 0);
        btnPreviousBookingStep.setAlpha(currentBookingStep > 0 ? 1f : 0.5f);

        if (currentBookingStep == TOTAL_BOOKING_STEPS - 1) {
            btnNextBookingStep.setText("Hoàn tất");
        } else {
            btnNextBookingStep.setText("Tiếp tục");
        }
    }

    private void applyIndicatorStyle(TextView indicator, int backgroundRes, int textColorRes) {
        indicator.setBackgroundResource(backgroundRes);
        indicator.setTextColor(ContextCompat.getColor(this, textColorRes));
    }

    // ============ MENU & LOGOUT ============
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        MenuItem adminItem = menu.findItem(R.id.action_admin_dashboard);
        MenuItem bookingsItem = menu.findItem(R.id.action_my_bookings);
        if (adminItem != null) adminItem.setVisible(false);
        if (bookingsItem != null) bookingsItem.setVisible(false);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        sessionManager.logout();
        ApiClient.resetClient();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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

        String bookingIdAfterTicket = extractAfterTicketSegment(content);
        if (!TextUtils.isEmpty(bookingIdAfterTicket)) {
            return bookingIdAfterTicket;
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
            String fragmentBookingId = extractAfterTicketSegment(uri.getFragment());
            if (!TextUtils.isEmpty(fragmentBookingId)) {
                return fragmentBookingId;
            }

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

    private String extractAfterTicketSegment(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        String lowerValue = value.toLowerCase(Locale.ROOT);
        int ticketIndex = lowerValue.indexOf("/ticket/");
        if (ticketIndex < 0) {
            return null;
        }

        String candidate = value.substring(ticketIndex + "/ticket/".length()).trim();
        if (TextUtils.isEmpty(candidate)) {
            return null;
        }

        int cutAt = candidate.length();
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (c == '/' || c == '?' || c == '#' || c == '&') {
                cutAt = i;
                break;
            }
        }

        String bookingId = candidate.substring(0, cutAt).trim();
        return TextUtils.isEmpty(bookingId) ? null : bookingId;
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

        String customerName = firstNonEmpty(currentScanResult.getCustomerName(), "N/A");
        String movieName = firstNonEmpty(currentScanResult.getMovieName(), "N/A");
        String showtime = firstNonEmpty(currentScanResult.getShowtime(), "N/A").replace("T", " ");
        String room = firstNonEmpty(currentScanResult.getRoom(), "N/A");
        String bookingId = firstNonEmpty(currentScanResult.getBookingId(), "N/A");
        Integer ticketsPrinted = currentScanResult.getTicketsPrinted();

        tvCustomerName.setText("Khách: " + customerName);
        tvMovieInfo.setText("Phim: " + movieName + "\nGiờ chiếu: " + showtime);

        List<String> seats = currentScanResult.getSeats();
        String seatsText = seats != null && !seats.isEmpty() ? String.join(", ", seats) : "N/A";
        tvSeatsInfo.setText("Ghế: " + seatsText);

        if (layoutTotalPriceRow != null) {
            layoutTotalPriceRow.setVisibility(View.GONE);
        }
        if (dividerTotalPrice != null) {
            dividerTotalPrice.setVisibility(View.GONE);
        }
        tvTotalPrice.setVisibility(View.GONE);

        boolean isCheckedIn = isScanAlreadyCheckedIn(currentScanResult);
        if (isCheckedIn) {
            tvCheckInStatus.setText("✓ Đã check-in");
            tvCheckInStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnConfirmCheckIn.setEnabled(false);
        } else {
            String rawStatus = firstNonEmpty(currentScanResult.getStatus(), "BOOKED").toUpperCase(Locale.ROOT);
            if ("PAID".equals(rawStatus) || "CONFIRMED".equals(rawStatus) || "BOOKED".equals(rawStatus)) {
                tvCheckInStatus.setText("Sẵn sàng check-in");
            } else {
                tvCheckInStatus.setText("Trạng thái: " + rawStatus);
            }
            tvCheckInStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnConfirmCheckIn.setEnabled(true);
        }

        String ticketCountLine = ticketsPrinted != null ? "\nSố vé in: " + ticketsPrinted : "";
        tvBookingDetails.setText("Mã đơn: " + bookingId +
                "\nPhòng: " + room +
                ticketCountLine);
    }

    private boolean isScanAlreadyCheckedIn(ScanResponse scanResponse) {
        if (scanResponse == null) {
            return false;
        }
        if (Boolean.TRUE.equals(scanResponse.getIsCheckedIn())) {
            return true;
        }
        String status = scanResponse.getStatus();
        if (TextUtils.isEmpty(status)) {
            return false;
        }
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        return "USED".equals(normalizedStatus) || "CHECKED_IN".equals(normalizedStatus);
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
