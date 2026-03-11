package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.MainActivity;
import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.LoginRequest;
import com.example.lab10.models.LoginResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private MovieApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    
    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Log.d("LOGIN", "HTTP code: " + response.code());
                Log.d("LOGIN", "body null: " + (response.body() == null));

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    Log.d("LOGIN", "api code: " + apiResponse.getCode());
                    Log.d("LOGIN", "api message: " + apiResponse.getMessage());
                    Log.d("LOGIN", "result null: " + (apiResponse.getResult() == null));
                    if (apiResponse.getResult() != null) {
                        Log.d("LOGIN", "token: " + apiResponse.getResult().getToken());
                    }

                    LoginResponse loginResponse = apiResponse.getResult();
                    String token = (loginResponse != null) ? loginResponse.getToken() : null;

                    // Chấp nhận mọi response HTTP 200 có token
                    if (token != null && !token.isEmpty()) {
                        ApiClient.setAuthToken(token);
                        sessionManager.createLoginSession(token);
                        // Lấy thông tin user để lưu userId
                        apiService.getMyInfo().enqueue(new retrofit2.Callback<ApiResponse<com.example.lab10.models.User>>() {
                            @Override
                            public void onResponse(retrofit2.Call<ApiResponse<com.example.lab10.models.User>> c,
                                                   retrofit2.Response<ApiResponse<com.example.lab10.models.User>> r) {
                                if (r.isSuccessful() && r.body() != null && r.body().getResult() != null) {
                                    sessionManager.saveUser(r.body().getResult());
                                    Log.d("LOGIN", "userId saved: " + r.body().getResult().getId());
                                }
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            }
                            @Override
                            public void onFailure(retrofit2.Call<ApiResponse<com.example.lab10.models.User>> c, Throwable t) {
                                Log.e("LOGIN", "getMyInfo failed: " + t.getMessage());
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            }
                        });
                    } else {
                        // Thử navigate nếu code == 1000 dù không có token
                        if (apiResponse.getCode() == 1000) {
                            sessionManager.createLoginSession("");
                            navigateToMain();
                        } else {
                            String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng nhập thất bại";
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e("LOGIN", "Error body: " + errorBody);
                    Toast.makeText(LoginActivity.this, "Lỗi " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.e("LOGIN", "onFailure: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
