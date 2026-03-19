package com.example.lab10;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.activities.AddMovieActivity;
import com.example.lab10.activities.AdminLoyaltyActivity;
import com.example.lab10.activities.AdminUserActivity;
import com.example.lab10.activities.LoginActivity;
import com.example.lab10.activities.LoyaltyActivity;
import com.example.lab10.activities.MovieDetailActivity;
import com.example.lab10.activities.MyBookingsActivity;
import com.example.lab10.activities.ProfileActivity;
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
        
        initViews();
        setupTabs();
        loadMoviesByRole();
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
            fabAddMovie.setVisibility(View.VISIBLE);
            loadAllMovies();
        } else {
            setTitle("Rạp Phim");
            tabLayout.setVisibility(View.VISIBLE);
            fabAddMovie.setVisibility(View.GONE);
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
            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
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
        Log.d("MAIN", "Movie clicked: " + movie.getTitle());
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        User user = sessionManager.getUser();
        boolean isAdmin = (user != null && user.isAdmin());
        
        // Quản lý các mục hiển thị động
        MenuItem bookingItem = menu.findItem(R.id.action_my_bookings);
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        MenuItem loyaltyItem = menu.findItem(R.id.action_loyalty);
        MenuItem manageUsersItem = menu.findItem(R.id.action_manage_users);
        MenuItem manageLoyaltyItem = menu.findItem(R.id.action_manage_loyalty);
        
        if (isAdmin) {
            // Đối với Admin
            if (bookingItem != null) bookingItem.setTitle("Đặt vé");
            if (profileItem != null) profileItem.setVisible(false);
            if (loyaltyItem != null) loyaltyItem.setVisible(false);
            if (manageUsersItem != null) manageUsersItem.setVisible(true);
            if (manageLoyaltyItem != null) manageLoyaltyItem.setVisible(true);
        } else {
            // Đối với User thường
            if (bookingItem != null) bookingItem.setTitle("Vé của tôi");
            if (profileItem != null) profileItem.setVisible(true);
            if (loyaltyItem != null) loyaltyItem.setVisible(true);
            if (manageUsersItem != null) manageUsersItem.setVisible(false);
            if (manageLoyaltyItem != null) manageLoyaltyItem.setVisible(false);
        }
        
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_my_bookings) {
            startActivity(new Intent(this, MyBookingsActivity.class));
            return true;
        } else if (id == R.id.action_loyalty) {
            startActivity(new Intent(this, LoyaltyActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_manage_users) {
            startActivity(new Intent(this, AdminUserActivity.class));
            return true;
        } else if (id == R.id.action_manage_loyalty) {
            startActivity(new Intent(this, AdminLoyaltyActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        sessionManager.logout();
        ApiClient.resetClient();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}