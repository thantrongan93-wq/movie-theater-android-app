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
import com.example.lab10.models.GoogleLoginRequest;
import com.example.lab10.models.LoginRequest;
import com.example.lab10.models.LoginResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private MovieApiService apiService;
    private GoogleSignInClient googleSignInClient;
    
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
        setupGoogleSignIn();
        setupListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleLogin = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        btnGoogleLogin.setOnClickListener(v -> startGoogleLogin());
        
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void startGoogleLogin() {
        String webClientId = getString(R.string.google_web_client_id).trim();
        if (webClientId.startsWith("YOUR_")) {
            Toast.makeText(this, "Ban can cau hinh google_web_client_id trong strings.xml", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
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
        
        setLoading(true);
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                handleAuthResponse(response, "Đăng nhập thành công");
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Log.e("LOGIN", "onFailure: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performGoogleBackendLogin(String idToken) {
        apiService.googleLogin(new GoogleLoginRequest(idToken)).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                handleGoogleAuthResponse(response, "Dang nhap Google thanh cong");
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                setLoading(false);
                Log.e("LOGIN", "googleLogin onFailure: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, " Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleAuthResponse(Response<ApiResponse<JsonElement>> response, String successMessage) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<JsonElement> apiResponse = response.body();
            JsonElement result = apiResponse.getResult();
            String token = extractTokenFromGoogleResult(result);

            if (token != null && !token.isEmpty()) {
                handleTokenLogin(token, successMessage);
                return;
            }

            setLoading(false);
            String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Dang nhap that bai";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(false);
        String errorBody = "";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (Exception ignored) {
            // Ignore parse error and fallback to status code only.
        }
        Log.e("LOGIN", "Google error body: " + errorBody);
        Toast.makeText(this, "Loi " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
    }

    private String extractTokenFromGoogleResult(JsonElement result) {
        if (result == null || result.isJsonNull()) {
            return null;
        }

        if (result.isJsonPrimitive()) {
            return result.getAsString();
        }

        if (result.isJsonObject()) {
            JsonObject obj = result.getAsJsonObject();

            if (obj.has("token") && !obj.get("token").isJsonNull()) {
                return obj.get("token").getAsString();
            }
            if (obj.has("accessToken") && !obj.get("accessToken").isJsonNull()) {
                return obj.get("accessToken").getAsString();
            }
            if (obj.has("jwt") && !obj.get("jwt").isJsonNull()) {
                return obj.get("jwt").getAsString();
            }
        }

        return null;
    }

    private void handleAuthResponse(Response<ApiResponse<LoginResponse>> response, String successMessage) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<LoginResponse> apiResponse = response.body();
            LoginResponse loginResponse = apiResponse.getResult();
            String token = (loginResponse != null) ? loginResponse.getToken() : null;

            if (token != null && !token.isEmpty()) {
                handleTokenLogin(token, successMessage);
            } else if (apiResponse.getCode() == 1000) {
                setLoading(false);
                sessionManager.createLoginSession("");
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                setLoading(false);
                String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Dang nhap that bai";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } else {
            setLoading(false);
            String errorBody = "";
            try {
                if (response.errorBody() != null) {
                    errorBody = response.errorBody().string();
                }
            } catch (Exception ignored) {
                // Ignore parse error and fallback to status code only.
            }
            Log.e("LOGIN", "Error body: " + errorBody);
            Toast.makeText(this, "Loi " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
        }
    }

    private void handleTokenLogin(String token, String successMessage) {
        ApiClient.setAuthToken(token);
        sessionManager.createLoginSession(token);

        apiService.getMyInfo().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    sessionManager.saveUser(response.body().getResult());
                }
                Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                setLoading(false);
                Log.e("LOGIN", "getMyInfo failed: " + t.getMessage());
                Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoogleLogin.setEnabled(!loading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            if (data == null) {
                setLoading(false);
                Toast.makeText(this, "Đăng nhập Google bị huỷ", Toast.LENGTH_SHORT).show();
                return;
            }

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account != null ? account.getIdToken() : null;
                if (idToken == null || idToken.isEmpty()) {
                    setLoading(false);
                    Toast.makeText(this, "Khong lay duoc idToken tu Google", Toast.LENGTH_LONG).show();
                    return;
                }

                performGoogleBackendLogin(idToken);
            } catch (ApiException e) {
                setLoading(false);
                int statusCode = e.getStatusCode();
                Log.e("LOGIN", "Google sign-in failed. statusCode=" + statusCode, e);
                if (statusCode == 12501) {
                    Toast.makeText(this, "Đăng nhập Google bị huỷ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Google lỗi (code " + statusCode + ")", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
    }
    
    private void navigateToMain() {
        User user = sessionManager.getUser();
        Class<?> destination;
        if (user != null) {
            if (user.isAdmin()) {
                destination = AdminDashboardActivity.class;
            } else if (user.isEmployee()) {
                destination = EmployeeDashboardActivity.class;
            } else {
                destination = MainActivity.class;
            }
        } else {
            destination = MainActivity.class;
        }

        Intent intent = new Intent(LoginActivity.this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
