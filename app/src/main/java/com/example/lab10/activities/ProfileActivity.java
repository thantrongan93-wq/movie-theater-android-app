package com.example.lab10.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lab10.R;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.LoyaltyResponse;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvTier, tvCurrentPoints, tvMaxPoints;
    private ProgressBar pbPoints;
    private TextInputEditText etFullName, etDob, etEmail, etPhone, etIdentityCard;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    
    private MovieApiService apiService;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        initViews();
        
        // Load data from session immediately for smooth UI
        currentUser = sessionManager.getUser();
        if (currentUser != null) {
            displayUser(currentUser);
        }
        
        loadProfileData();
        loadLoyaltyData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("HỒ SƠ CỦA TÔI");
        }

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvTier = findViewById(R.id.tv_tier);
        tvCurrentPoints = findViewById(R.id.tv_current_points);
        tvMaxPoints = findViewById(R.id.tv_max_points);
        pbPoints = findViewById(R.id.pb_points);

        etFullName = findViewById(R.id.et_full_name);
        etDob = findViewById(R.id.et_dob);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etIdentityCard = findViewById(R.id.et_identity_card);

        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);

        findViewById(R.id.btn_update_profile).setOnClickListener(v -> updateProfile());
        findViewById(R.id.btn_change_password).setOnClickListener(v -> showChangePasswordDialog());
        
        findViewById(R.id.btn_support).setOnClickListener(v -> 
            Toast.makeText(this, "Yêu cầu hỗ trợ đã được gửi!", Toast.LENGTH_SHORT).show());
    }

    private void loadProfileData() {
        apiService.getMyInfo().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getResult();
                    if (user != null) {
                        currentUser = user;
                        displayUser(currentUser);
                        sessionManager.saveUser(currentUser);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Log.e("PROFILE_LOAD", "Failed", t);
            }
        });
    }

    private void displayUser(User user) {
        if (user == null) return;
        tvProfileName.setText(user.getFullName() != null ? user.getFullName() : "User");
        etFullName.setText(user.getFullName());
        etDob.setText(user.getDateOfBirth());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etIdentityCard.setText(user.getIdentityCard());

        String gender = user.getGender();
        if ("MALE".equalsIgnoreCase(gender)) rbMale.setChecked(true);
        else if ("FEMALE".equalsIgnoreCase(gender)) rbFemale.setChecked(true);
        else rbOther.setChecked(true);
    }

    private void loadLoyaltyData() {
        apiService.getMyLoyalty().enqueue(new Callback<ApiResponse<LoyaltyResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoyaltyResponse>> call, @NonNull Response<ApiResponse<LoyaltyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoyaltyResponse loyalty = response.body().getResult();
                    if (loyalty != null) {
                        tvTier.setText(loyalty.getTierName() != null ? loyalty.getTierName() : "MEMBER");
                        int points = loyalty.getTotalPoints() != null ? loyalty.getTotalPoints() : 0;
                        tvCurrentPoints.setText(String.valueOf(points));
                        
                        Integer nextPoints = loyalty.getNextTierPoints();
                        int max = (nextPoints != null && nextPoints > 0) ? points + nextPoints : Math.max(points, 1000);
                        tvMaxPoints.setText(String.valueOf(max));
                        pbPoints.setMax(max);
                        pbPoints.setProgress(points);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoyaltyResponse>> call, @NonNull Throwable t) {
                Log.e("LOYALTY_LOAD", "Failed", t);
            }
        });
    }

    private void updateProfile() {
        if (currentUser == null) {
            currentUser = sessionManager.getUser();
        }
        if (currentUser == null) return;

        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Họ tên không được để trống");
            return;
        }

        // Map UI to object
        currentUser.setFullName(fullName);
        currentUser.setDateOfBirth(etDob.getText().toString().trim());
        currentUser.setPhoneNumber(etPhone.getText().toString().trim());
        currentUser.setIdentityCard(etIdentityCard.getText().toString().trim());

        String gender = "OTHER";
        if (rbMale.isChecked()) gender = "MALE";
        else if (rbFemale.isChecked()) gender = "FEMALE";
        currentUser.setGender(gender);

        // Ensure ID is present from session if missing in object
        if (currentUser.getId() == null) {
            currentUser.setId(sessionManager.getUserId());
        }

        apiService.updateProfile(currentUser).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    sessionManager.saveUser(currentUser);
                    tvProfileName.setText(currentUser.getFullName());
                } else {
                    Toast.makeText(ProfileActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOld = view.findViewById(R.id.et_old_password);
        TextInputEditText etNew = view.findViewById(R.id.et_new_password);
        TextInputEditText etConf = view.findViewById(R.id.et_confirm_password);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(view)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String oldP = etOld.getText().toString();
                    String newP = etNew.getText().toString();
                    String confP = etConf.getText().toString();

                    if (TextUtils.isEmpty(oldP) || TextUtils.isEmpty(newP)) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newP.equals(confP)) {
                        Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JsonObject json = new JsonObject();
                    json.addProperty("oldPassword", oldP);
                    json.addProperty("newPassword", newP);

                    apiService.changePassword(json).enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                            Toast.makeText(ProfileActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
