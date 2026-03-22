package com.example.lab10.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMovieActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";

    private Toolbar toolbar;
    private EditText etTitle, etReleaseDate, etProduction, etDuration, etVersion, 
                     etDirector, etActors, etGenres, etTrailerUrl, etPosterUrl,
                     etAgeRating, etDescription, etRating, etPurchasePrice, etPurchaseDate;
    private Button btnSave;
    private ProgressBar progressBar;
    private MovieApiService apiService;
    private SessionManager sessionManager;
    private Movie editingMovie;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        User user = sessionManager.getUser();
        if (user == null || !user.isAdmin()) {
            Toast.makeText(this, "Chỉ Admin mới có quyền quản lý phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editingMovie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        isEditMode = editingMovie != null && editingMovie.getId() != null;

        initViews();
        setupToolbar();
        if (isEditMode) {
            fillMovieData(editingMovie);
        }

        btnSave.setOnClickListener(v -> saveMovie());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etReleaseDate = findViewById(R.id.et_release_date);
        etProduction = findViewById(R.id.et_production);
        etDuration = findViewById(R.id.et_duration);
        etVersion = findViewById(R.id.et_version);
        etDirector = findViewById(R.id.et_director);
        etActors = findViewById(R.id.et_actors);
        etGenres = findViewById(R.id.et_genres);
        etTrailerUrl = findViewById(R.id.et_trailer_url);
        etPosterUrl = findViewById(R.id.et_poster_url);
        etAgeRating = findViewById(R.id.et_age_rating);
        etDescription = findViewById(R.id.et_description);
        etRating = findViewById(R.id.et_rating);
        etPurchasePrice = findViewById(R.id.et_purchase_price);
        etPurchaseDate = findViewById(R.id.et_purchase_date);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Cập nhật phim" : "Thêm phim mới");
        }

        btnSave.setText(isEditMode ? "Cập nhật phim" : "Lưu phim");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveMovie() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tên phim");
            return;
        }

        String version = etVersion.getText().toString().trim();
        if (version.isEmpty()) {
            etVersion.setError("Vui lòng nhập phiên bản (2D/3D)");
            return;
        }

        String purchasePrice = etPurchasePrice.getText().toString().trim();
        if (purchasePrice.isEmpty()) {
            etPurchasePrice.setError("Vui lòng nhập giá mua phim");
            return;
        }

        String purchaseDate = etPurchaseDate.getText().toString().trim();
        if (purchaseDate.isEmpty()) {
            etPurchaseDate.setError("Vui lòng nhập ngày mua (YYYY-MM-DD)");
            return;
        }

        Movie movie = buildMovieFromForm();

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (isEditMode && editingMovie != null && editingMovie.getId() != null) {
            apiService.updateMovie(editingMovie.getId(), movie).enqueue(new Callback<ApiResponse<Movie>>() {
                @Override
                public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                    handleSaveResponse(response, "Cập nhật phim thành công!");
                }

                @Override
                public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                    handleSaveFailure(t);
                }
            });
            return;
        }

        apiService.createMovie(movie).enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                handleSaveResponse(response, "Thêm phim thành công!");
            }

            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                handleSaveFailure(t);
            }
        });
    }

    private Movie buildMovieFromForm() {
        Movie movie = new Movie();
        movie.setTitle(etTitle.getText().toString().trim());
        movie.setReleaseDate(etReleaseDate.getText().toString().trim());
        movie.setProductionCompany(etProduction.getText().toString().trim());
        movie.setRunningTime(etDuration.getText().toString().trim());
        movie.setVersion(etVersion.getText().toString().trim());
        movie.setDirector(etDirector.getText().toString().trim());
        
        String actorsStr = etActors.getText().toString().trim();
        if (!actorsStr.isEmpty()) {
            movie.setActors(Arrays.asList(actorsStr.split("\\s*,\\s*")));
        }

        String genresStr = etGenres.getText().toString().trim();
        if (!genresStr.isEmpty()) {
            movie.setGenres(Arrays.asList(genresStr.split("\\s*,\\s*")));
        }

        movie.setTrailerUrl(etTrailerUrl.getText().toString().trim());
        movie.setPosterUrl(etPosterUrl.getText().toString().trim());
        movie.setAgeRating(etAgeRating.getText().toString().trim());
        movie.setDescription(etDescription.getText().toString().trim());

        try {
            String ratingStr = etRating.getText().toString().trim();
            if (!ratingStr.isEmpty()) {
                movie.setRating(Double.parseDouble(ratingStr));
            }
        } catch (NumberFormatException ignored) {}

        try {
            String purchasePriceStr = etPurchasePrice.getText().toString().trim();
            if (!purchasePriceStr.isEmpty()) {
                movie.setPurchasePrice(Double.parseDouble(purchasePriceStr));
            }
        } catch (NumberFormatException ignored) {}

        movie.setPurchaseDate(etPurchaseDate.getText().toString().trim());

        return movie;
    }

    private void fillMovieData(Movie movie) {
        etTitle.setText(movie.getTitle());
        etReleaseDate.setText(movie.getReleaseDate());
        etProduction.setText(movie.getProductionCompany());
        etDuration.setText(movie.getDuration());
        etVersion.setText(movie.getVersion());
        etDirector.setText(movie.getDirector());
        etActors.setText(movie.getActors());

        List<String> genres = movie.getGenres();
        if (genres != null && !genres.isEmpty()) {
            etGenres.setText(String.join(", ", genres));
        }

        etTrailerUrl.setText(movie.getTrailerUrl());
        etPosterUrl.setText(movie.getPosterUrl());
        etAgeRating.setText(movie.getAgeRating());
        etDescription.setText(movie.getDescription());

        if (movie.getRating() != null) {
            etRating.setText(String.valueOf(movie.getRating()));
        }

        if (movie.getPurchasePrice() != null) {
            etPurchasePrice.setText(String.valueOf(movie.getPurchasePrice()));
        }

        etPurchaseDate.setText(movie.getPurchaseDate());
    }

    private void handleSaveResponse(Response<ApiResponse<Movie>> response, String successMessage) {
        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        if (response.isSuccessful() && response.body() != null) {
            Toast.makeText(AddMovieActivity.this, successMessage, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            String errorMsg = parseErrorMessage(response);
            Toast.makeText(AddMovieActivity.this, "Lỗi " + response.code() + ": " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return "Dữ liệu gửi lên chưa hợp lệ";
            }

            String errorJson = response.errorBody().string();
            if (errorJson == null || errorJson.trim().isEmpty()) {
                return "Dữ liệu gửi lên chưa hợp lệ";
            }

            JSONObject json = new JSONObject(errorJson);
            if (json.has("message")) {
                return json.optString("message", "Dữ liệu gửi lên chưa hợp lệ");
            }

            if (json.has("error")) {
                return json.optString("error", "Dữ liệu gửi lên chưa hợp lệ");
            }

            return errorJson;
        } catch (Exception e) {
            return "Dữ liệu gửi lên chưa hợp lệ";
        }
    }

    private void handleSaveFailure(Throwable t) {
        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        Toast.makeText(AddMovieActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
