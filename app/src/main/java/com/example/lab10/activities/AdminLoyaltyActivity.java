package com.example.lab10.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab10.R;
import com.example.lab10.adapters.PointHistoryAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.AdjustPointsRequest;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.LoyaltyInfo;
import com.example.lab10.models.PointHistory;
import com.example.lab10.models.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoyaltyActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;
    private View cardUserInfo;
    private TextView tvFullName, tvEmail, tvCurrentPoints;
    private RecyclerView rvHistory;
    private PointHistoryAdapter adapter;

    private MovieApiService apiService;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_loyalty);

        initViews();
        setupToolbar();
        setupRecyclerView();
        initData();
        setupListeners();
        
        handleIntentData();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        tilSearch = findViewById(R.id.tilSearch);
        cardUserInfo = findViewById(R.id.cardUserInfo);
        tvFullName = findViewById(R.id.tvAdminUserFullName);
        tvEmail = findViewById(R.id.tvAdminUserEmail);
        tvCurrentPoints = findViewById(R.id.tvAdminCurrentPoints);
        rvHistory = findViewById(R.id.rvAdminPointHistory);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý điểm thưởng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    private void initData() {
        apiService = ApiClient.getApiService();
    }

    private void handleIntentData() {
        long userId = getIntent().getLongExtra("user_id", -1);
        String userEmail = getIntent().getStringExtra("user_email");
        
        if (userId != -1) {
            if (userEmail != null) etSearch.setText(userEmail);
            // Thay vì load trực tiếp, ta dùng query email để lấy object User đầy đủ
            if (userEmail != null) {
                searchUser(userEmail);
            }
        }
    }

    private void setupListeners() {
        tilSearch.setEndIconOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                searchUser(query);
            }
        });

        findViewById(R.id.btnAdjustPoints).setOnClickListener(v -> showAdjustPointsDialog());
    }

    private void searchUser(String query) {
        Call<ApiResponse<User>> call;
        if (query.contains("@")) {
            call = apiService.searchUserByEmail(query);
        } else {
            call = apiService.searchUserByPhone(query);
        }

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    selectedUser = response.body().getResult();
                    displayUserInfo(selectedUser);
                    loadUserLoyalty(selectedUser.getId());
                } else {
                    Toast.makeText(AdminLoyaltyActivity.this, "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
                    cardUserInfo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(AdminLoyaltyActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        cardUserInfo.setVisibility(View.VISIBLE);
        tvFullName.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        tvEmail.setText(user.getEmail());
    }

    private void loadUserLoyalty(Long userId) {
        apiService.getLoyaltyInfo(userId).enqueue(new Callback<ApiResponse<LoyaltyInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoyaltyInfo>> call, Response<ApiResponse<LoyaltyInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvCurrentPoints.setText(String.valueOf(response.body().getResult().getPoints()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoyaltyInfo>> call, Throwable t) {
                // Ignore
            }
        });

        apiService.getAdminPointHistory(userId, 0, 50).enqueue(new Callback<ApiResponse<List<PointHistory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PointHistory>>> call, Response<ApiResponse<List<PointHistory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body().getResult());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PointHistory>>> call, Throwable t) {
                // Ignore
            }
        });
    }

    private void showAdjustPointsDialog() {
        if (selectedUser == null) {
            Toast.makeText(this, "Vui lòng chọn khách hàng trước", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_adjust_points, null);

        TextInputLayout tilPoints = view.findViewById(R.id.tilAdjustPoints);
        TextInputEditText etPoints = view.findViewById(R.id.etAdjustPoints);
        TextInputEditText etReason = view.findViewById(R.id.etAdjustReason);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmAdjust);

        btnConfirm.setOnClickListener(v -> {
            String pStr = etPoints.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            boolean isValid = true;
            if (TextUtils.isEmpty(pStr)) {
                tilPoints.setError("Nhập số điểm");
                isValid = false;
            } else {
                tilPoints.setError(null);
            }

            if (TextUtils.isEmpty(reason)) {
                etReason.setError("Nhập lý do");
                isValid = false;
            }

            if (!isValid) return;

            try {
                int points = Integer.parseInt(pStr);
                AdjustPointsRequest request = new AdjustPointsRequest(points, reason);

                apiService.adjustPoints(selectedUser.getId(), request).enqueue(new Callback<ApiResponse<LoyaltyInfo>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoyaltyInfo>> call, Response<ApiResponse<LoyaltyInfo>> response) {
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(AdminLoyaltyActivity.this, "Điều chỉnh điểm thành công", Toast.LENGTH_SHORT).show();
                            loadUserLoyalty(selectedUser.getId());
                        } else {
                            Toast.makeText(AdminLoyaltyActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoyaltyInfo>> call, Throwable t) {
                        Toast.makeText(AdminLoyaltyActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                tilPoints.setError("Số điểm không hợp lệ");
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }
}