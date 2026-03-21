package com.example.lab10.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import com.example.lab10.models.ShowtimeRequest;
import com.example.lab10.utils.SessionManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.ShowtimeAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String TAG = "MovieDetailActivity";
    
    private ImageView ivPoster, ivBack, ivPlayTrailer;
    private View vOverlay;
    private WebView wvTrailer;
    private TextView tvTitle, tvGenre, tvDuration, tvRating, tvDirector, tvLanguage, tvAgeRating, tvDescription;
    private RecyclerView rvShowtimes;
    private ProgressBar progressBar;
    
    private Movie movie;
    private ShowtimeAdapter showtimeAdapter;
    private MovieApiService apiService;
    private Button btnAddShowtime;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);
        
        movie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        if (movie == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        displayMovieDetails();
        loadFullMovieDetails();
        loadShowtimes();
    }
    
    private void initViews() {
        ivPoster = findViewById(R.id.iv_poster);
        ivBack = findViewById(R.id.iv_back);
        ivPlayTrailer = findViewById(R.id.iv_play_trailer);
        vOverlay = findViewById(R.id.v_overlay);
        wvTrailer = findViewById(R.id.wv_trailer);
        tvTitle = findViewById(R.id.tv_title);
        tvGenre = findViewById(R.id.tv_genre);
        tvDuration = findViewById(R.id.tv_duration);
        tvRating = findViewById(R.id.tv_rating);
        tvDirector = findViewById(R.id.tv_director);
        tvLanguage = findViewById(R.id.tv_language);
        tvAgeRating = findViewById(R.id.tv_age_rating);
        tvDescription = findViewById(R.id.tv_description);
        rvShowtimes = findViewById(R.id.rv_showtimes);
        progressBar = findViewById(R.id.progress_bar);
        btnAddShowtime = findViewById(R.id.btn_add_showtime);
        com.example.lab10.models.User user = sessionManager.getUser();
        if (user != null && user.isAdmin()) {
            btnAddShowtime.setVisibility(View.VISIBLE);
        }
        btnAddShowtime.setOnClickListener(v -> showAdminShowtimeDialog());

        ivBack.setOnClickListener(v -> finish());

        // Cấu hình WebView tối ưu
        WebSettings webSettings = wvTrailer.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Cập nhật UserAgent mới và chuẩn hơn
        String newUserAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.64 Mobile Safari/537.36";
        webSettings.setUserAgentString(newUserAgent);

        wvTrailer.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // Đảm bảo luôn load trong WebView
            }
        });
        wvTrailer.setWebChromeClient(new WebChromeClient());

        ivPlayTrailer.setOnClickListener(v -> {
            String trailerUrl = movie.getTrailerUrl();
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                String videoId = extractYoutubeId(trailerUrl);
                if (videoId != null) {
                    wvTrailer.setVisibility(View.VISIBLE);
                    ivPlayTrailer.setVisibility(View.GONE);
                    vOverlay.setVisibility(View.GONE);
                    
                    // Sử dụng youtube-nocookie.com và các tham số chuẩn
                    String embedUrl = "https://www.youtube-nocookie.com/embed/" + videoId + "?autoplay=1&modestbranding=1&rel=0";
                    
                    String html = "<!DOCTYPE html><html>" +
                            "<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
                            "<body style=\"margin:0;padding:0;background-color:black;\">" +
                            "<div style=\"position:relative;padding-bottom:56.25%;height:0;overflow:hidden;\">" +
                            "<iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "\" " +
                            "frameborder=\"0\" style=\"position:absolute;top:0;left:0;width:100%;height:100%;\" " +
                            "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" " +
                            "allowfullscreen></iframe>" +
                            "</div></body></html>";
                            
                    wvTrailer.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "utf-8", null);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Phim chưa có trailer", Toast.LENGTH_SHORT).show();
            }
        });

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        rvShowtimes.setNestedScrollingEnabled(false);

        boolean isAdmin = sessionManager.getUser() != null && sessionManager.getUser().isAdmin();
        showtimeAdapter = new ShowtimeAdapter(new ArrayList<>(), this::onShowtimeClick, isAdmin);
        rvShowtimes.setAdapter(showtimeAdapter);
    }

    private String extractYoutubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    private void loadFullMovieDetails() {
        apiService.getMovieById(movie.getId()).enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie fullMovie = response.body().getResult();
                    if (fullMovie != null) {
                        movie = fullMovie;
                        displayMovieDetails();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {}
        });
    }

    private void loadShowtimes() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getShowtimeDetailsByMovie(movie.getId()).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Showtime> showtimes = response.body().getResult();
                    if (showtimes != null && !showtimes.isEmpty()) {
                        showtimeAdapter.updateData(showtimes);
                    } else {
                        Log.d(TAG, "Showtimes list is empty");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load showtimes", t);
            }
        });
    }
    
    private void displayMovieDetails() {
        ImageLoader.loadImageWithPlaceholder(ivPoster, movie.getPosterUrl(), R.drawable.ic_launcher_foreground);
        tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "");
        tvGenre.setText(movie.getGenre());
        tvDuration.setText(movie.getDuration() + " phút");
        tvRating.setText(movie.getRating() != null ? String.valueOf(movie.getRating()) : "N/A");
        tvDirector.setText("Đạo diễn: " + (movie.getDirector() != null ? movie.getDirector() : "Chưa cập nhật"));
        tvLanguage.setText("Sản xuất: " + (movie.getProductionCompany() != null ? movie.getProductionCompany() : "Chưa cập nhật"));
        
        String age = movie.getAgeRating();
        if (age != null && !age.isEmpty()) {
            tvAgeRating.setText(age);
            tvAgeRating.setVisibility(View.VISIBLE);
        } else {
            tvAgeRating.setVisibility(View.GONE);
        }
        
        String actors = movie.getActors();
        String description = movie.getDescription() != null ? movie.getDescription() : "";
        if (actors != null && !actors.isEmpty()) {
            description = "Diễn viên: " + actors + "\n\n" + description;
        }
        tvDescription.setText(description);

        if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().isEmpty()) {
            ivPlayTrailer.setVisibility(View.VISIBLE);
        } else {
            ivPlayTrailer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wvTrailer != null) {
            wvTrailer.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (wvTrailer != null) {
            wvTrailer.destroy();
        }
        super.onDestroy();
    }
    
    private void onShowtimeClick(Showtime showtime) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    public void showAdminShowtimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm lịch chiếu");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_showtime_form, null);
        builder.setView(dialogView);

        EditText etRoomId    = dialogView.findViewById(R.id.et_room_id);
        EditText etStartTime = dialogView.findViewById(R.id.et_start_time);
        EditText etEndTime   = dialogView.findViewById(R.id.et_end_time);
        EditText etPrice     = dialogView.findViewById(R.id.et_price);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            try {
                Long roomId   = Long.parseLong(etRoomId.getText().toString().trim());
                String startT = etStartTime.getText().toString().trim();
                String endT   = etEndTime.getText().toString().trim();
                Double price  = Double.parseDouble(etPrice.getText().toString().trim());

                ShowtimeRequest request = new ShowtimeRequest(
                        movie.getId(), roomId, startT, endT, price);
                createShowtime(request);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập đúng định dạng", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void createShowtime(ShowtimeRequest request) {
        apiService.createShowtime(request).enqueue(new Callback<ApiResponse<Showtime>>() {
            @Override
            public void onResponse(Call<ApiResponse<Showtime>> call,
                                   Response<ApiResponse<Showtime>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MovieDetailActivity.this,
                            "Thêm lịch chiếu thành công", Toast.LENGTH_SHORT).show();
                    loadShowtimes();
                } else {
                    Toast.makeText(MovieDetailActivity.this,
                            "Thêm thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Showtime>> call, Throwable t) {
                Toast.makeText(MovieDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteShowtime(Long showtimeId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa lịch chiếu này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiService.deleteShowtime(showtimeId).enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call,
                                               Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MovieDetailActivity.this,
                                        "Đã xóa lịch chiếu", Toast.LENGTH_SHORT).show();
                                loadShowtimes();
                            } else {
                                Toast.makeText(MovieDetailActivity.this,
                                        "Xóa thất bại (lỗi " + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            Toast.makeText(MovieDetailActivity.this,
                                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

