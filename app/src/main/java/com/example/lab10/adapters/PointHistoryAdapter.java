package com.example.lab10.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.PointHistory;
import com.example.lab10.utils.DateTimeUtils;

import java.util.List;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private List<PointHistory> historyList;

    public PointHistoryAdapter(List<PointHistory> historyList) {
        this.historyList = historyList;
    }

    public void updateData(List<PointHistory> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointHistory history = historyList.get(position);
        

        int displayId = getItemCount() - position;
        holder.tvId.setText(String.valueOf(displayId));
        
        holder.tvType.setText(history.getType());
        
        int amount = (history.getAmount() != null) ? history.getAmount() : 0;
        if (amount >= 0) {
            holder.tvAmount.setText("+" + amount);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvAmount.setText(String.valueOf(amount));
            holder.tvAmount.setTextColor(Color.RED);
        }
        
        String formattedDate = DateTimeUtils.formatToWebDateTime(history.getDateTime());
        holder.tvDate.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvType, tvAmount, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_id);
            tvType = itemView.findViewById(R.id.tv_type);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
