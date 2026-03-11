package com.example.lab10.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Seat;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {
    
    private List<Seat> seats;
    private OnSeatSelectionChangedListener listener;
    
    public interface OnSeatSelectionChangedListener {
        void onSeatSelectionChanged();
    }
    
    public SeatAdapter(List<Seat> seats, OnSeatSelectionChangedListener listener) {
        this.seats = seats;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seats.get(position);
        holder.bind(seat, listener);
    }
    
    @Override
    public int getItemCount() {
        return seats.size();
    }
    
    public void updateSeats(List<Seat> newSeats) {
        this.seats = newSeats;
        notifyDataSetChanged();
    }
    
    public List<Seat> getSelectedSeats() {
        List<Seat> selectedSeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isSelected()) {
                selectedSeats.add(seat);
            }
        }
        return selectedSeats;
    }
    
    class SeatViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvSeatNumber;
        
        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvSeatNumber = itemView.findViewById(R.id.tv_seat_number);
        }
        
        public void bind(Seat seat, OnSeatSelectionChangedListener listener) {
            String row = seat.getRowNumber() != null ? seat.getRowNumber() : "";
            String num = seat.getSeatNumber() != null ? seat.getSeatNumber() : "";
            tvSeatNumber.setText(row + num);
            
            boolean available = seat.getIsAvailable() != null ? seat.getIsAvailable() : false;
            if (!available) {
                // Seat is booked
                cardView.setCardBackgroundColor(Color.parseColor("#CCCCCC"));
                tvSeatNumber.setTextColor(Color.WHITE);
                itemView.setEnabled(false);
            } else if (seat.isSelected()) {
                // Seat is selected
                cardView.setCardBackgroundColor(Color.parseColor("#4CAF50"));
                tvSeatNumber.setTextColor(Color.WHITE);
            } else {
                // Seat is available
                cardView.setCardBackgroundColor(Color.WHITE);
                tvSeatNumber.setTextColor(Color.BLACK);
            }
            
            itemView.setOnClickListener(v -> {
                if (seat.getIsAvailable() != null && seat.getIsAvailable()) {
                    seat.setSelected(!seat.isSelected());
                    notifyItemChanged(getAdapterPosition());
                    if (listener != null) {
                        listener.onSeatSelectionChanged();
                    }
                }
            });
        }
    }
}
