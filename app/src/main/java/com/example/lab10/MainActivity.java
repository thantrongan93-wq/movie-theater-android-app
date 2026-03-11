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

        Log.d("MAIN", "isLoggedIn=" + sessionManager.isLoggedIn() + " token=" + sessionManager.getToken());

        apiService = ApiClient.getApiService();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.d("MAIN", "Not logged in -> redirect to Login");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        initViews();
        loadMovies();
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
    
    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getActiveMovies().enqueue(new Callback<ApiResponse<PageResponse<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Movie>>> call, Response<ApiResponse<PageResponse<Movie>>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("MOVIES", "HTTP code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResponse<Movie>> apiResponse = response.body();
                    Log.d("MOVIES", "api message: " + apiResponse.getMessage());
                    PageResponse<Movie> page = apiResponse.getResult();
                    List<Movie> movies = page != null ? page.getMovies() : null;
                    Log.d("MOVIES", "movies null: " + (movies == null) + " size: " + (movies != null ? movies.size() : 0));

                    if (movies == null || movies.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        movieAdapter.updateData(movies);
                    }
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("MOVIES", "Error: " + response.code() + " body=" + err);
                        Toast.makeText(MainActivity.this, "Lỗi " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Không tải được danh sách phim", Toast.LENGTH_SHORT).show();
                    }
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Movie>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e("MOVIES", "onFailure: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
            ApiClient.resetClient(); // xoa JWT token
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}