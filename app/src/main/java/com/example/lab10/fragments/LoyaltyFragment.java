package com.example.lab10.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab10.R;
import com.example.lab10.adapters.PointHistoryAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.LoyaltyInfo;
import com.example.lab10.models.PointHistory;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoyaltyFragment extends Fragment {

    private TextView tvPoints, tvTier, tvPointsToNext;
    private LinearProgressIndicator tierProgressBar;
    private LinearProgressIndicator loadingProgress;
    private RecyclerView rvHistory;
    private View layoutEmpty;
    private PointHistoryAdapter adapter;
    private NestedScrollView scrollView;

    private MovieApiService apiService;
    private SessionManager sessionManager;
    private User currentUser;

    private int currentPage = 0;
    private final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loyalty, container, false);

        initViews(view);
        setupRecyclerView();
        initData();
        loadLoyaltyInfo();
        loadPointHistory(true);
        setupPagination();

        return view;
    }

    private void initViews(View view) {
        tvPoints = view.findViewById(R.id.tvPoints);
        tvTier = view.findViewById(R.id.tvTier);
        tvPointsToNext = view.findViewById(R.id.tvPointsToNext);
        tierProgressBar = view.findViewById(R.id.tierProgressBar);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        rvHistory = view.findViewById(R.id.rvPointHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        scrollView = view.findViewById(R.id.scrollViewLoyalty);

        if (tvPointsToNext != null) tvPointsToNext.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);
        rvHistory.setNestedScrollingEnabled(false); // Vì đã có NestedScrollView bao ngoài
    }

    private void initData() {
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(requireContext());
        currentUser = sessionManager.getUser();
    }

    private void setupPagination() {
        if (scrollView == null) return;
        
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Kiểm tra khi scroll đến cuối
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                if (!isLoading && !isLastPage) {
                    loadPointHistory(false);
                }
            }
        });
    }

    private void loadLoyaltyInfo() {
        if (currentUser == null || currentUser.getId() == null) return;

        apiService.getLoyaltyInfo(currentUser.getId()).enqueue(new Callback<ApiResponse<LoyaltyInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoyaltyInfo>> call, Response<ApiResponse<LoyaltyInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoyaltyInfo info = response.body().getResult();
                    if (info != null) displayLoyaltyInfo(info);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<LoyaltyInfo>> call, Throwable t) {}
        });
    }

    private void loadPointHistory(boolean isInitial) {
        if (currentUser == null || isLoading || isLastPage) return;

        if (isInitial) {
            currentPage = 0;
            isLastPage = false;
            adapter.clear();
        }

        isLoading = true;
        showLoading(true);

        apiService.getPointHistory(currentPage, PAGE_SIZE).enqueue(new Callback<ApiResponse<List<PointHistory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PointHistory>>> call, Response<ApiResponse<List<PointHistory>>> response) {
                isLoading = false;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PointHistory> history = response.body().getResult();
                    if (history == null || history.isEmpty()) {
                        isLastPage = true;
                        if (isInitial) {
                            rvHistory.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        adapter.addData(history);
                        rvHistory.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        currentPage++;
                        
                        // Nếu số lượng trả về ít hơn PAGE_SIZE thì nghĩa là trang cuối
                        if (history.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PointHistory>>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
            }
        });
    }

    private void displayLoyaltyInfo(LoyaltyInfo info) {
        if (tvPoints != null) tvPoints.setText(NumberFormat.getNumberInstance(Locale.US).format(info.getPoints()));
        if (tvTier != null) tvTier.setText(info.getTier());

        if (info.getNextTier() != null && !info.getNextTier().isEmpty()) {
            if (tvPointsToNext != null) {
                tvPointsToNext.setVisibility(View.VISIBLE);
                tvPointsToNext.setText(info.getPointsToNextTier() + " điểm nữa để lên " + info.getNextTier());
            }
            
            int total = info.getPoints() + info.getPointsToNextTier();
            if (tierProgressBar != null && total > 0) {
                tierProgressBar.setProgress((int) ((info.getPoints() * 100.0f) / total));
            }
        } else {
            if (tvPointsToNext != null) {
                tvPointsToNext.setVisibility(View.VISIBLE);
                tvPointsToNext.setText("Bạn đã đạt hạng cao nhất");
            }
            if (tierProgressBar != null) tierProgressBar.setProgress(100);
        }
    }

    private void showLoading(boolean show) {
        if (loadingProgress != null) loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}