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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.activities.LoginActivity;
import com.example.lab10.activities.MovieDetailActivity;
import com.example.lab10.activities.MyBookingsActivity;
import com.example.lab10.adapters.MovieAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvMovies;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MovieAdapter movieAdapter;
    private MovieApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sessionManager = new SessionManager(this);

        // Restore JWT token vào ApiClient (khi app bị restart)
        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        initViews();
        loadMoviesByRole(); // Gọi hàm phân quyền load phim
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        rvMovies = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(new ArrayList<>(), this::onMovieClick);
        rvMovies.setAdapter(movieAdapter);
    }
    
    private void loadMoviesByRole() {
        User user = sessionManager.getUser();
        
        if (user != null && user.isAdmin()) {
            Log.d("MAIN", "Role: ADMIN -> loading all movies");
            setTitle("Admin - Tất cả phim");
            loadAllMovies();
        } else {
            Log.d("MAIN", "Role: USER -> loading upcoming movies");
            setTitle("Phim đang chiếu");
            loadUpcomingMovies();
        }
    }

    private void loadAllMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getActiveMovies().enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
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
        // Gọi API /api/movies/upcomingMovies với page=0, size=10
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

    private void handleMovieResponse(Response<ApiResponse<PageResponse<Movie>>> response) {
        progressBar.setVisibility(View.GONE);
        if (response.isSuccessful() && response.body() != null) {
            PageResponse<Movie> page = response.body().getResult();
            List<Movie> movies = (page != null) ? page.getMovies() : null;
            
            if (movies == null || movies.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                movieAdapter.updateData(movies);
            }
        } else {
            Toast.makeText(this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void handleFailure(Throwable t) {
        progressBar.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
    
    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_my_bookings) {
            Intent intent = new Intent(this, MyBookingsActivity.class);
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
