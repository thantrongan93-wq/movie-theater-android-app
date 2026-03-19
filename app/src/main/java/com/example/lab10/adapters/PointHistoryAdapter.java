package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab10.R;
import com.example.lab10.models.PointHistory;
import java.util.ArrayList;
import java.util.List;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private List<PointHistory> historyList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointHistory item = historyList.get(position);

        if (item == null) return;

        if (holder.tvDate != null) {
            holder.tvDate.setText(item.getDate() != null ? item.getDate() : "");
        }
        if (holder.tvReason != null) {
            holder.tvReason.setText(item.getReason() != null ? item.getReason() : "");
        }
        if (holder.tvBalance != null) {
            holder.tvBalance.setText("Số dư: " + item.getBalance());
        }

        // Xử lý hiển thị điểm
        int points = item.getPoints();
        if (holder.tvPoints != null) {
            if (points >= 0) {
                holder.tvPoints.setText("+" + points);
                holder.tvPoints.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
                if (holder.viewIndicator != null) {
                    holder.viewIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
                }
            } else {
                holder.tvPoints.setText(String.valueOf(points));
                holder.tvPoints.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                if (holder.viewIndicator != null) {
                    holder.viewIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public void setData(List<PointHistory> newData) {
        this.historyList = newData != null ? new ArrayList<>(newData) : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Thêm dữ liệu mới vào cuối danh sách (phân trang)
     */
    public void addData(List<PointHistory> moreData) {
        if (moreData != null && !moreData.isEmpty()) {
            int startPos = this.historyList.size();
            this.historyList.addAll(moreData);
            notifyItemRangeInserted(startPos, moreData.size());
        }
    }

    public void clear() {
        this.historyList.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvReason, tvPoints, tvBalance;
        View viewIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            viewIndicator = itemView.findViewById(R.id.viewIndicator);
        }
    }
}