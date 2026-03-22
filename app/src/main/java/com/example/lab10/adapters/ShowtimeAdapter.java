package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    
    private List<Showtime> showtimes;
    private OnShowtimeClickListener listener;
    private boolean isAdmin;
    
    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime);
    }

    public ShowtimeAdapter(List<Showtime> showtimes, OnShowtimeClickListener listener, boolean isAdmin) {
        this.showtimes = showtimes != null ? showtimes : new ArrayList<>();
        this.listener  = listener;
        this.isAdmin   = isAdmin;
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
        holder.bind(showtimes.get(position), listener, isAdmin);
    }
    
    @Override
    public int getItemCount() {
        return showtimes.size();
    }
    
    public void updateData(List<Showtime> newShowtimes) {
        this.showtimes = newShowtimes != null ? newShowtimes : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvTime, tvTheater, tvPrice, tvAvailableSeats;
        private ImageButton ibDelete;
        
        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTheater = itemView.findViewById(R.id.tv_theater);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAvailableSeats = itemView.findViewById(R.id.tv_available_seats);
            ibDelete = itemView.findViewById(R.id.ib_delete);
        }

        public void bind(Showtime showtime, OnShowtimeClickListener listener, boolean isAdmin) {
            tvDate.setText(showtime.getShowDate() != null ? DateTimeUtils.formatDate(showtime.getShowDate()) : "");
            
            String start = showtime.getStartTime() != null ? DateTimeUtils.formatTime(showtime.getStartTime()) : "";
            String end   = showtime.getEndTime() != null ? DateTimeUtils.formatTime(showtime.getEndTime()) : "";
            tvTime.setText(start + (end.isEmpty() ? "" : " - " + end));

            if (showtime.getTheater() != null) tvTheater.setText(showtime.getTheater().getName());
            else if (showtime.getCinemaRoomName() != null) tvTheater.setText(showtime.getCinemaRoomName());
            else tvTheater.setText("Phòng " + showtime.getRoomId());

            tvPrice.setText(CurrencyUtils.formatPrice(showtime.getPrice()));
            tvAvailableSeats.setText(showtime.getAvailableSeats() != null ? showtime.getAvailableSeats() + " ghế trống" : "");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onShowtimeClick(showtime);
            });

            if (isAdmin && ibDelete != null) {
                ibDelete.setVisibility(View.VISIBLE);
                ibDelete.setOnClickListener(v -> {
                    if (itemView.getContext() instanceof com.example.lab10.activities.MovieDetailActivity) {
                        ((com.example.lab10.activities.MovieDetailActivity) itemView.getContext()).deleteShowtime(showtime.getId());
                    }
                });
            } else if (ibDelete != null) {
                ibDelete.setVisibility(View.GONE);
            }
        }
    }
}
