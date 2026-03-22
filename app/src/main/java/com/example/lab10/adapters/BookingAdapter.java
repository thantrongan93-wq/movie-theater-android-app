package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Booking;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings;
    private OnCancelBookingListener listener;
    private OnBookingClickListener clickListener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }
    public interface OnCancelBookingListener {
        void onCancelBooking(Booking booking);
    }
    
    public BookingAdapter(List<Booking> bookings, OnCancelBookingListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }
    public BookingAdapter(List<Booking> bookings, OnCancelBookingListener listener,
                          OnBookingClickListener clickListener) {
        this.bookings = bookings;
        this.listener = listener;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking, listener, clickListener);
    }
    
    @Override
    public int getItemCount() {
        return bookings.size();
    }
    
    public void updateData(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }
    
    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }
    
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookingCode, tvMovieTitle, tvShowDateTime, tvSeats, tvTotalPrice, tvStatus;
        private Button btnCancel;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvShowDateTime = itemView.findViewById(R.id.tv_show_datetime);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }

        public void bind(Booking booking, OnCancelBookingListener listener,
                         OnBookingClickListener clickListener) {
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onBookingClick(booking);
            });
            tvBookingCode.setText("Mã: " + booking.getBookingCode());
            
            if (booking.getShowtime() != null && booking.getShowtime().getMovie() != null) {
                tvMovieTitle.setText(booking.getShowtime().getMovie().getTitle());
                tvShowDateTime.setText(DateTimeUtils.formatDate(booking.getShowtime().getShowDate()) + " " +
                                      DateTimeUtils.formatTime(booking.getShowtime().getShowTime()));
            }
            
            if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
                String seatNumbers = booking.getSeats().stream()
                        .map(seat -> seat.getRowNumber() + seat.getSeatNumber())
                        .collect(Collectors.joining(", "));
                tvSeats.setText("Ghế: " + seatNumbers);
            }
            
            tvTotalPrice.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
            String status = booking.getStatus() != null ? booking.getStatus() : "";
            tvStatus.setText(status);

            switch (status) {
                case "CONFIRMED":
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // xanh
                    break;
                case "CANCELLED":
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336")); // đỏ
                    break;
                case "PENDING":
                case "BOOKING":
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")); // cam
                    break;
                default:
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#333333")); // đen
                    break;
            }

            if ("PENDING".equals(status) || "BOOKING".equals(status)) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelBooking(booking);
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }
    }
}
