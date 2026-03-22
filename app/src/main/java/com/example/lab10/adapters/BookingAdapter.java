package com.example.lab10.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.activities.PaymentActivity;
import com.example.lab10.models.Booking;
import com.example.lab10.utils.CurrencyUtils;
import com.example.lab10.utils.DateTimeUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings;
    private OnBookingActionListener listener;
    
    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
    }
    
    public BookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
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
        holder.bind(booking, listener);
    }
    
    @Override
    public int getItemCount() {
        return bookings.size();
    }
    
    public void updateData(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }
    
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookingCode, tvMovieTitle, tvShowDateTime, tvSeats, tvTotalPrice, tvStatus;
        private Button btnCancel, btnPay;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvShowDateTime = itemView.findViewById(R.id.tv_show_datetime);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnPay = itemView.findViewById(R.id.btn_pay);
        }
        
        public void bind(Booking booking, OnBookingActionListener listener) {
            String code = booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
            tvBookingCode.setText("Mã: " + code);
            
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
            tvStatus.setText(booking.getStatus());
            
            // Log trạng thái để debug
            Log.d("BookingAdapter", "Booking: " + code + " | Status: [" + booking.getStatus() + "]");

            // Sử dụng equalsIgnoreCase để so sánh không phân biệt hoa thường
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                btnCancel.setVisibility(View.VISIBLE);
                btnPay.setVisibility(View.VISIBLE);
                
                btnCancel.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelBooking(booking);
                });
                
                btnPay.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), PaymentActivity.class);
                    intent.putExtra(PaymentActivity.EXTRA_BOOKING, booking);
                    intent.putExtra("AUTO_START_PAYMENT", true);
                    itemView.getContext().startActivity(intent);
                });
            } else {
                btnCancel.setVisibility(View.GONE);
                btnPay.setVisibility(View.GONE);
            }
        }
    }
}
