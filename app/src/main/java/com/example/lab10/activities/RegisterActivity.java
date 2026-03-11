package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private EditText etUsername, etFullName, etEmail, etPhone, etDateOfBirth,
            etIdentityCard, etAddress, etPassword, etConfirmPassword;
    private RadioGroup rgGender;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private MovieApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etIdentityCard = findViewById(R.id.et_identity_card);
        etAddress = findViewById(R.id.et_address);
        rgGender = findViewById(R.id.rg_gender);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());
        
        tvLogin.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void register() {
        String username        = etUsername.getText().toString().trim();
        String fullName        = etFullName.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String phone           = etPhone.getText().toString().trim();
        String dateOfBirth     = etDateOfBirth.getText().toString().trim();
        String identityCard    = etIdentityCard.getText().toString().trim();
        String address         = etAddress.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Xác định giới tính
        String gender = (rgGender.getCheckedRadioButtonId() == R.id.rb_female) ? "FEMALE" : "MALE";

        // Validation bắt buộc
        if (username.isEmpty()) { etUsername.setError("Bắt buộc"); etUsername.requestFocus(); return; }
        if (fullName.isEmpty())  { etFullName.setError("Bắt buộc"); etFullName.requestFocus(); return; }
        if (email.isEmpty())     { etEmail.setError("Bắt buộc"); etEmail.requestFocus(); return; }
        if (phone.isEmpty())     { etPhone.setError("Bắt buộc"); etPhone.requestFocus(); return; }
        if (password.isEmpty())  { etPassword.setError("Bắt buộc"); etPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(password);
        user.setConfirmPassword(confirmPassword);
        user.setGender(gender);
        if (!dateOfBirth.isEmpty()) user.setDateOfBirth(dateOfBirth);
        if (!identityCard.isEmpty()) user.setIdentityCard(identityCard);
        if (!address.isEmpty()) user.setAddress(address);

        apiService.register(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng ký thất bại";
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
