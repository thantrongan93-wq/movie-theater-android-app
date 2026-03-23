package com.example.lab10.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.PointHistoryAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.PageResponse;
import com.example.lab10.models.PointHistory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private PointHistoryAdapter adapter;
    private MovieApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvHistory = findViewById(R.id.rv_point_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PointHistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(adapter);

        apiService = ApiClient.getApiService();
        loadHistory();
    }

    private void loadHistory() {
        apiService.getMyPointHistory(0, 50).enqueue(new Callback<ApiResponse<PageResponse<PointHistory>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageResponse<PointHistory>>> call, @NonNull Response<ApiResponse<PageResponse<PointHistory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<PointHistory> page = response.body().getResult();
                    if (page != null) {
                        List<PointHistory> list = page.getContent();
                        if (list != null) {
                            adapter.updateData(list);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PageResponse<PointHistory>>> call, @NonNull Throwable t) {
                Toast.makeText(PointHistoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
