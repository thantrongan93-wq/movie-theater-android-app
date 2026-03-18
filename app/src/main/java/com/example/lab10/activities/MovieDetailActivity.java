package com.example.lab10.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
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
    private View layoutAdminActions;
    private WebView wvTrailer;
    private Button btnEditMovie, btnDeleteMovie;
    private TextView tvTitle, tvGenre, tvDuration, tvRating, tvDirector, tvLanguage, tvAgeRating, tvDescription;
    private RecyclerView rvShowtimes;
    private ProgressBar progressBar;
    
    private Movie movie;
    private ShowtimeAdapter showtimeAdapter;
    private MovieApiService apiService;
    private SessionManager sessionManager;
    private boolean isAdmin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        User user = sessionManager.getUser();
        isAdmin = user != null && user.isAdmin();
        
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
        layoutAdminActions = findViewById(R.id.layout_admin_actions);
        btnEditMovie = findViewById(R.id.btn_edit_movie);
        btnDeleteMovie = findViewById(R.id.btn_delete_movie);
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
        
        ivBack.setOnClickListener(v -> finish());

        if (isAdmin) {
            layoutAdminActions.setVisibility(View.VISIBLE);
            btnEditMovie.setOnClickListener(v -> openEditMovieScreen());
            btnDeleteMovie.setOnClickListener(v -> showDeleteConfirmDialog());
        } else {
            layoutAdminActions.setVisibility(View.GONE);
        }

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
        showtimeAdapter = new ShowtimeAdapter(new ArrayList<>(), this::onShowtimeClick);
        rvShowtimes.setAdapter(showtimeAdapter);
    }

    private void openEditMovieScreen() {
        Intent intent = new Intent(this, AddMovieActivity.class);
        intent.putExtra(AddMovieActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    private void showDeleteConfirmDialog() {
        if (movie == null || movie.getId() == null) {
            Toast.makeText(this, "Không tìm thấy ID phim", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa phim")
                .setMessage("Bạn có chắc muốn xóa phim này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteMovie())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteMovie() {
        if (!isAdmin || movie == null || movie.getId() == null) {
            Toast.makeText(this, "Bạn không có quyền xóa phim", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnDeleteMovie.setEnabled(false);

        apiService.deleteMovie(movie.getId()).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                progressBar.setVisibility(View.GONE);
                btnDeleteMovie.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(MovieDetailActivity.this, "Xóa phim thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Xóa phim thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnDeleteMovie.setEnabled(true);
                Toast.makeText(MovieDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        if (movie == null || movie.getId() == null) {
            Log.e(TAG, "Cannot load showtimes: movieId is null");
            showtimeAdapter.updateData(new ArrayList<>());
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (isAdmin) {
            Log.d(TAG, "ADMIN load showtimes by showtime-details/movie/" + movie.getId());
            apiService.getShowtimeDetailsByMovie(movie.getId()).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Showtime> showtimes = response.body().getResult();
                        if (showtimes != null && !showtimes.isEmpty()) {
                            showtimeAdapter.updateData(showtimes);
                        } else {
                            Log.d(TAG, "Admin showtimes list is empty");
                            showtimeAdapter.updateData(new ArrayList<>());
                        }
                    } else {
                        Log.w(TAG, "Admin load showtimes failed HTTP " + response.code());
                        showtimeAdapter.updateData(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Admin failed to load showtimes", t);
                    showtimeAdapter.updateData(new ArrayList<>());
                }
            });
            return;
        }

        Log.d(TAG, "USER load showtimes by showtimes/movie then showtime-details/{id}: movieId=" + movie.getId());
        loadUserShowtimesByDetailId();
    }

    private void loadUserShowtimesByDetailId() {
        apiService.getShowtimesByMovie(movie.getId()).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "User load showtimes/movie failed HTTP " + response.code());
                    showtimeAdapter.updateData(new ArrayList<>());
                    return;
                }

                List<Showtime> baseShowtimes = response.body().getResult();
                if (baseShowtimes == null || baseShowtimes.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "User showtimes/movie list is empty");
                    showtimeAdapter.updateData(new ArrayList<>());
                    return;
                }

                List<Long> detailIds = new ArrayList<>();
                for (Showtime showtime : baseShowtimes) {
                    if (showtime != null && showtime.getId() != null) {
                        detailIds.add(showtime.getId());
                    }
                }

                if (detailIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "User showtimes/movie does not contain ids for detail API");
                    showtimeAdapter.updateData(baseShowtimes);
                    return;
                }

                List<Showtime> detailShowtimes = new ArrayList<>();
                final int total = detailIds.size();
                final int[] done = {0};

                for (Long detailId : detailIds) {
                    apiService.getShowtimeDetailById(detailId).enqueue(new Callback<ApiResponse<Showtime>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Showtime>> call, Response<ApiResponse<Showtime>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                                detailShowtimes.add(response.body().getResult());
                            }
                            completeDetailFetch(done, total, detailShowtimes, baseShowtimes);
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Showtime>> call, Throwable t) {
                            Log.w(TAG, "User showtime-details/{id} failed id=" + detailId + " msg=" + t.getMessage());
                            completeDetailFetch(done, total, detailShowtimes, baseShowtimes);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "User failed to load showtimes/movie", t);
                showtimeAdapter.updateData(new ArrayList<>());
            }
        });
    }

    private void completeDetailFetch(int[] done, int total, List<Showtime> detailShowtimes, List<Showtime> baseShowtimes) {
        done[0]++;
        if (done[0] < total) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        if (!detailShowtimes.isEmpty()) {
            showtimeAdapter.updateData(detailShowtimes);
        } else {
            showtimeAdapter.updateData(baseShowtimes != null ? baseShowtimes : new ArrayList<>());
        }
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
    protected void onResume() {
        super.onResume();
        if (movie != null && movie.getId() != null) {
            loadFullMovieDetails();
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
}
