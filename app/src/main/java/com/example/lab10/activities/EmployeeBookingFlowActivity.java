package com.example.lab10.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab10.R;
import com.example.lab10.adapters.BookingShowtimeAdapter;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.api.ApiClient;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.ShowtimeGroup;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeBookingFlowActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeBookingFlow";
    private static final int TOTAL_STEPS = 5;
    private static final int[] UPCOMING_PAGE_SIZES = {50, 20, 10};

    private int currentStep = 0;
    private Long selectedMovieId = null;
    private Long selectedShowtimeId = null;
    
    private TextView[] stepIndicators;
    private LinearLayout[] stepContainers;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnRestartFlow;
    
    private RecyclerView rvShowtimes;
    private ProgressBar progressShowtimes;
    private TextView tvShowtimesEmpty;
    private BookingShowtimeAdapter showtimeAdapter;
    
    private RecyclerView rvMovies;
    private ProgressBar progressMovies;
    private TextView tvMoviesEmpty;
    private MovieAdapter movieAdapter;
    
    private MovieApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_booking_flow);

        apiService = ApiClient.getApiService();
        setupToolbar();
        initViews();
        setupActions();
        renderStep();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_booking_flow);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Employee Booking Flow");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
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

        btnPrevious = findViewById(R.id.btn_previous_step);
        btnNext = findViewById(R.id.btn_next_step);
        btnRestartFlow = findViewById(R.id.btn_restart_flow);
        
        // Showtimes UI
        rvShowtimes = findViewById(R.id.rv_showtimes);
        progressShowtimes = findViewById(R.id.progress_showtimes);
        tvShowtimesEmpty = findViewById(R.id.tv_showtimes_empty);
        
        if (rvShowtimes != null) {
            rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
            showtimeAdapter = new BookingShowtimeAdapter(new ArrayList<>(), this::onShowtimeClick);
            rvShowtimes.setAdapter(showtimeAdapter);
        }
        
        // Movies UI
        rvMovies = findViewById(R.id.rv_movies);
        progressMovies = findViewById(R.id.progress_movies);
        tvMoviesEmpty = findViewById(R.id.tv_movies_empty);
        
        if (rvMovies != null) {
            rvMovies.setLayoutManager(new LinearLayoutManager(this));
            movieAdapter = new MovieAdapter(new ArrayList<>(), this::onMovieClick);
            rvMovies.setAdapter(movieAdapter);
        }
    }

    private void setupActions() {
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                renderStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            // Validate before moving to next step
            if (currentStep == 0) {
                // Step 1: Validate movie is selected
                if (selectedMovieId == null) {
                    Toast.makeText(this, "Vui lòng chọn phim", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (currentStep == 1) {
                // Step 2: Validate showtime is selected
                if (selectedShowtimeId == null) {
                    Toast.makeText(this, "Vui lòng chọn suất chiếu", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            if (currentStep < TOTAL_STEPS - 1) {
                currentStep++;
                renderStep();
            } else {
                finish();
            }
        });

        btnRestartFlow.setOnClickListener(v -> {
            currentStep = 0;
            selectedMovieId = null;
            selectedShowtimeId = null;
            renderStep();
        });
    }

    private void onMovieClick(Movie movie) {
        if (movie != null && movie.getId() != null) {
            selectedMovieId = movie.getId();
            movieAdapter.setSelectedMovieId(movie.getId());
            Log.d(TAG, "Selected movie ID: " + selectedMovieId);
            Toast.makeText(this, "Đã chọn: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onShowtimeClick(Long showtimeId) {
        selectedShowtimeId = showtimeId;
        showtimeAdapter.setSelectedShowtimeId(showtimeId);
        Log.d(TAG, "Selected showtime ID: " + showtimeId);
    }

    private void loadMovies() {
        loadMovies(0);
    }

    private void loadMovies(int retryIndex) {
        if (progressMovies != null) {
            progressMovies.setVisibility(View.VISIBLE);
        }
        if (rvMovies != null) {
            rvMovies.setVisibility(View.GONE);
        }
        if (tvMoviesEmpty != null) {
            tvMoviesEmpty.setVisibility(View.GONE);
        }

        int size = UPCOMING_PAGE_SIZES[Math.min(retryIndex, UPCOMING_PAGE_SIZES.length - 1)];
        apiService.getUpcomingMovies(0, size).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (progressMovies != null) {
                    progressMovies.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    PageResponse<Movie> pageResponse = response.body().getResult();
                    java.util.List<Movie> movies = pageResponse.getMovies();
                    if (movies == null || movies.isEmpty()) {
                        if (tvMoviesEmpty != null) {
                            tvMoviesEmpty.setVisibility(View.VISIBLE);
                        }
                        if (movieAdapter != null) {
                            movieAdapter.updateData(new ArrayList<>());
                        }
                    } else {
                        if (rvMovies != null) {
                            rvMovies.setVisibility(View.VISIBLE);
                        }
                        if (movieAdapter != null) {
                            movieAdapter.updateData(movies);
                        }
                    }
                } else {
                    if (tvMoviesEmpty != null) {
                        tvMoviesEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (t instanceof EOFException && retryIndex < UPCOMING_PAGE_SIZES.length - 1) {
                    Log.w(TAG, "loadMovies EOF, retry with smaller page size", t);
                    loadMovies(retryIndex + 1);
                    return;
                }
                if (progressMovies != null) {
                    progressMovies.setVisibility(View.GONE);
                }
                if (tvMoviesEmpty != null) {
                    tvMoviesEmpty.setVisibility(View.VISIBLE);
                }
                Log.e(TAG, "loadMovies failed", t);
            }
        });
    }

    private void renderStep() {
        for (int i = 0; i < stepContainers.length; i++) {
            stepContainers[i].setVisibility(i == currentStep ? View.VISIBLE : View.GONE);
        }

        for (int i = 0; i < stepIndicators.length; i++) {
            if (i < currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_done, android.R.color.white);
            } else if (i == currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_active, android.R.color.white);
            } else {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_idle, android.R.color.darker_gray);
            }
        }

        btnPrevious.setEnabled(currentStep > 0);
        btnPrevious.setAlpha(currentStep > 0 ? 1f : 0.5f);

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("Đóng");
        } else {
            btnNext.setText("Tiếp tục");
        }
        
        // Load movies if entering step 1
        if (currentStep == 0) {
            loadMovies();
        }
        
        // Load showtimes if entering step 2
        if (currentStep == 1 && selectedMovieId != null) {
            loadShowtimesForMovie(selectedMovieId);
        }
    }

    private void loadShowtimesForMovie(Long movieId) {
        if (progressShowtimes != null) {
            progressShowtimes.setVisibility(View.VISIBLE);
        }
        if (rvShowtimes != null) {
            rvShowtimes.setVisibility(View.GONE);
        }
        if (tvShowtimesEmpty != null) {
            tvShowtimesEmpty.setVisibility(View.GONE);
        }

        apiService.getMovieShowtimes(movieId).enqueue(new Callback<ApiResponse<List<ShowtimeGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShowtimeGroup>>> call, Response<ApiResponse<List<ShowtimeGroup>>> response) {
                if (progressShowtimes != null) {
                    progressShowtimes.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShowtimeGroup> groups = response.body().getResult();
                    if (groups == null || groups.isEmpty()) {
                        if (tvShowtimesEmpty != null) {
                            tvShowtimesEmpty.setVisibility(View.VISIBLE);
                        }
                        if (showtimeAdapter != null) {
                            showtimeAdapter.updateGroupData(new ArrayList<>());
                        }
                    } else {
                        if (rvShowtimes != null) {
                            rvShowtimes.setVisibility(View.VISIBLE);
                        }
                        if (showtimeAdapter != null) {
                            showtimeAdapter.updateGroupData(groups);
                        }
                    }
                } else {
                    if (tvShowtimesEmpty != null) {
                        tvShowtimesEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ShowtimeGroup>>> call, Throwable t) {
                if (progressShowtimes != null) {
                    progressShowtimes.setVisibility(View.GONE);
                }
                if (tvShowtimesEmpty != null) {
                    tvShowtimesEmpty.setVisibility(View.VISIBLE);
                }
                Log.e(TAG, "loadShowtimesForMovie failed", t);
                Toast.makeText(EmployeeBookingFlowActivity.this, 
                    "Lỗi tải suất chiếu: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyIndicatorStyle(TextView indicator, int backgroundRes, int textColorRes) {
        indicator.setBackgroundResource(backgroundRes);
        indicator.setTextColor(ContextCompat.getColor(this, textColorRes));
    }
}
