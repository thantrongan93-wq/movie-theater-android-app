package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.lab10.R;
import com.example.lab10.adapters.AdminUserAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.User;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserActivity extends AppCompatActivity implements AdminUserAdapter.OnUserClickListener {

    private RecyclerView rvUsers;
    private AdminUserAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private MovieApiService apiService;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        initViews();
        setupToolbar();
        setupRecyclerView();
        initData();
        loadAllUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvAdminUsers);
        swipeRefresh = findViewById(R.id.swipeRefreshUsers);
        etSearch = findViewById(R.id.etSearchUser);
        
        swipeRefresh.setOnRefreshListener(this::loadAllUsers);
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý người dùng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void initData() {
        apiService = ApiClient.getApiService();
    }

    private void loadAllUsers() {
        swipeRefresh.setRefreshing(true);
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body().getResult());
                } else {
                    Toast.makeText(AdminUserActivity.this, "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminUserActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        // Chuyển sang màn hình Quản lý Loyalty cho User này
        Intent intent = new Intent(this, AdminLoyaltyActivity.class);
        intent.putExtra("user_id", user.getId());
        intent.putExtra("user_email", user.getEmail());
        startActivity(intent);
    }

    @Override
    public void onUserLongClick(User user) {
        String[] options = {"Đổi quyền thành ADMIN", "Đổi quyền thành USER", "Xóa người dùng"};
        new AlertDialog.Builder(this)
                .setTitle("Quản lý người dùng")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) updateRole(user.getId(), "ADMIN");
                    else if (which == 1) updateRole(user.getId(), "USER");
                    else if (which == 2) showDeleteConfirm(user);
                }).show();
    }

    private void updateRole(Long userId, String role) {
        apiService.updateUserRole(userId, role).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    loadAllUsers();
                    Toast.makeText(AdminUserActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(AdminUserActivity.this, "Lỗi cập nhật quyền", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirm(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa " + user.getFullName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser(Long userId) {
        apiService.deleteUser(userId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    loadAllUsers();
                    Toast.makeText(AdminUserActivity.this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminUserActivity.this, "Lỗi xóa người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}