package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.lab10.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_MOVIE = "extra_movie";
    
    private ImageView ivPoster, ivBack;
    private TextView tvTitle, tvGenre, tvDuration, tvRating, tvDirector, tvLanguage, tvAgeRating, tvDescription;
    private RecyclerView rvShowtimes;
    private ProgressBar progressBar;
    
    private Movie movie;
    private ShowtimeAdapter showtimeAdapter;
    private MovieApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        
        apiService = ApiClient.getApiService();
        
        movie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        if (movie == null) {
            Toast.makeText(this, "Movie data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        displayMovieDetails();
        loadShowtimes();
    }
    
    private void initViews() {
        ivPoster = findViewById(R.id.iv_poster);
        ivBack = findViewById(R.id.iv_back);
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
        
        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        showtimeAdapter = new ShowtimeAdapter(new ArrayList<>(), this::onShowtimeClick);
        rvShowtimes.setAdapter(showtimeAdapter);
    }
    
    private void displayMovieDetails() {
        ImageLoader.loadImageWithPlaceholder(ivPoster, movie.getPosterUrl(), R.drawable.ic_launcher_foreground);
        tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "");
        String genre = movie.getGenre();
        tvGenre.setText(genre != null && !genre.isEmpty() ? genre : "Chưa cập nhật");
        tvDuration.setText(movie.getDuration() != null ? movie.getDuration() + " phút" : "Chưa cập nhật");
        tvRating.setText(movie.getAgeRestriction() != null ? "[" + movie.getAgeRestriction() + "]" : "");
        tvDirector.setText("Đạo diễn: " + (movie.getDirector() != null ? movie.getDirector() : "Chưa cập nhật"));
        tvLanguage.setText("Quốc gia: " + (movie.getCountry() != null ? movie.getCountry() : "Chưa cập nhật"));
        tvAgeRating.setText(movie.getAgeRestriction() != null ? "Giới hạn: " + movie.getAgeRestriction() : "");
        tvDescription.setText(movie.getDescription() != null ? movie.getDescription() : "");
    }
    
    private void loadShowtimes() {
        progressBar.setVisibility(View.VISIBLE);

        // Ưu tiên getShowtimeDetailsByMovie để lấy showtimeDetailId (cần cho booking)
        android.util.Log.d("SHOWTIMES", "Loading showtime details for movie: " + movie.getId());
        
        apiService.getShowtimeDetailsByMovie(movie.getId()).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("SHOWTIMES", "Details HTTP code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Showtime>> apiResponse = response.body();
                    List<Showtime> showtimes = apiResponse.getResult();
                    android.util.Log.d("SHOWTIMES", "Details showtimes: " + (showtimes != null ? showtimes.size() : 0));
                    
                    if (showtimes != null && !showtimes.isEmpty()) {
                        showtimeAdapter.updateData(showtimes);
                        return;
                    }
                }
                // Fallback nếu endpoint detail không có data
                android.util.Log.d("SHOWTIMES", "Fallback to regular showtimes");
                loadShowtimesFallback();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("SHOWTIMES", "Details failed: " + t.getMessage());
                loadShowtimesFallback();
            }
        });
    }
    
    private void loadShowtimesFallback() {
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getShowtimesByMovie(movie.getId()).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("SHOWTIMES", "Fallback HTTP code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Showtime> showtimes = response.body().getResult();
                    android.util.Log.d("SHOWTIMES", "Fallback showtimes: " + (showtimes != null ? showtimes.size() : 0));
                    
                    if (showtimes != null && !showtimes.isEmpty()) {
                        showtimeAdapter.updateData(showtimes);
                    } else {
                        Toast.makeText(MovieDetailActivity.this, "Không có lịch chiếu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        android.util.Log.e("SHOWTIMES", "Error " + response.code() + ": " + errorBody);
                    } catch (Exception ignored) {}
                    Toast.makeText(MovieDetailActivity.this, "Không tải được lịch chiếu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("SHOWTIMES", "Fallback failed: " + t.getMessage());
                Toast.makeText(MovieDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void onShowtimeClick(Showtime showtime) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }
}
