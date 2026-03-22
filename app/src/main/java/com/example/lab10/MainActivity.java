package com.example.lab10;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lab10.activities.BookingHistoryActivity;
import com.example.lab10.utils.NotificationHelper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.activities.AdminDashboardActivity;
import com.example.lab10.activities.AddMovieActivity;
import com.example.lab10.activities.LoginActivity;
import com.example.lab10.activities.MovieDetailActivity;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvMovies;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAddMovie;
    private MovieAdapter movieAdapter;
    private MovieApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sessionManager = new SessionManager(this);

        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermissionIfNeeded();
        
        initViews();
        setupTabs();
        loadMoviesByRole();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NOTIFICATION", "POST_NOTIFICATIONS permission granted");
            } else {
                Log.d("NOTIFICATION", "POST_NOTIFICATIONS permission denied");
            }
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        tabLayout = findViewById(R.id.tab_layout);
        rvMovies = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        fabAddMovie = findViewById(R.id.fab_add_movie);
        
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(new ArrayList<>(), this::onMovieClick);
        rvMovies.setAdapter(movieAdapter);

        fabAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMovieActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        if (tabLayout == null) return;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    loadUpcomingMovies();
                } else if (position == 1) {
                    loadComingSoonMovies();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void loadMoviesByRole() {
        User user = sessionManager.getUser();
        
        if (user != null && user.isAdmin()) {
            setTitle("Admin - Quản lý phim");
            tabLayout.setVisibility(View.GONE);
            fabAddMovie.setVisibility(View.VISIBLE); // Hiện nút thêm cho Admin
            loadAllMovies();
        } else {
            setTitle("Rạp Phim");
            tabLayout.setVisibility(View.VISIBLE);
            fabAddMovie.setVisibility(View.GONE); // Ẩn nút thêm cho User
            loadUpcomingMovies();
        }
    }

    private void loadAllMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getActiveMovies(0, 30).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                handleMovieResponse(response);
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }
    
    private void loadUpcomingMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getUpcomingMovies(0, 10).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                handleMovieResponse(response);
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void loadComingSoonMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getComingSoonMovies(0, 10).enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                handleMovieResponse(response);
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void handleMovieResponse(Response<ApiResponse<PageResponse<Movie>>> response) {
        progressBar.setVisibility(View.GONE);
        if (response.isSuccessful() && response.body() != null) {
            PageResponse<Movie> page = response.body().getResult();
            List<Movie> movies = (page != null) ? page.getMovies() : new ArrayList<>();
            if (movies == null || movies.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                movieAdapter.updateData(new ArrayList<>());
            } else {
                tvEmpty.setVisibility(View.GONE);
                movieAdapter.updateData(movies);
            }
        } else {
            movieAdapter.updateData(new ArrayList<>());
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void handleFailure(Throwable t) {
        progressBar.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        movieAdapter.updateData(new ArrayList<>());
        Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
    
    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadMoviesByRole();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        User user = sessionManager != null ? sessionManager.getUser() : null;
        MenuItem adminItem = menu.findItem(R.id.action_admin_dashboard);
        if (adminItem != null) {
            adminItem.setVisible(user != null && user.isAdmin());
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_admin_dashboard) {
            User user = sessionManager.getUser();
            if (user != null && user.isAdmin()) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_my_bookings) {
            Intent intent = new Intent(this, BookingHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
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
}
