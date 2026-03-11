package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;

import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    
    private List<Showtime> showtimes;
    private OnShowtimeClickListener listener;
    
    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime);
    }
    
    public ShowtimeAdapter(List<Showtime> showtimes, OnShowtimeClickListener listener) {
        this.showtimes = showtimes;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimes.get(position);
        holder.bind(showtime, listener);
    }
    
    @Override
    public int getItemCount() {
        return showtimes.size();
    }
    
    public void updateData(List<Showtime> newShowtimes) {
        this.showtimes = newShowtimes;
        notifyDataSetChanged();
    }
    
    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvTime, tvTheater, tvPrice, tvAvailableSeats;
        
        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTheater = itemView.findViewById(R.id.tv_theater);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAvailableSeats = itemView.findViewById(R.id.tv_available_seats);
        }
        
        public void bind(Showtime showtime, OnShowtimeClickListener listener) {
            String date = showtime.getShowDate();
            tvDate.setText(date != null ? DateTimeUtils.formatDate(date) : "");
            // Dùng startTime (field thực tế từ API)
            String start = showtime.getStartTime() != null ? DateTimeUtils.formatTime(showtime.getStartTime()) : "";
            String end   = showtime.getEndTime()   != null ? DateTimeUtils.formatTime(showtime.getEndTime())   : "";
            tvTime.setText(start + (!end.isEmpty() ? " - " + end : ""));

            if (showtime.getTheater() != null) {
                tvTheater.setText(showtime.getTheater().getName());
            } else if (showtime.getRoom() != null) {
                tvTheater.setText("Phòng: " + showtime.getRoom().getName());
            } else if (showtime.getCinemaRoomName() != null) {
                tvTheater.setText(showtime.getCinemaRoomName());
            } else if (showtime.getRoomId() != null) {
                tvTheater.setText("Phòng: " + showtime.getRoomId());
            } else {
                tvTheater.setText("");
            }

            tvPrice.setText(CurrencyUtils.formatPrice(showtime.getPrice()));

            Integer available = showtime.getAvailableSeats();
            tvAvailableSeats.setText(available != null ? available + " ghế trống" : "");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onShowtimeClick(showtime);
            });
        }
    }
}
