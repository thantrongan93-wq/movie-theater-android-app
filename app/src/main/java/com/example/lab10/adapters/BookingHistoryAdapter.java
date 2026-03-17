package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.BookingHistoryResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.utils.CurrencyUtils;

import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private List<BookingHistoryResponse> bookings;

    public BookingHistoryAdapter(List<BookingHistoryResponse> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingHistoryResponse booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateData(List<BookingHistoryResponse> newData) {
        this.bookings = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookingCode, tvMovieTitle, tvShowDateTime, tvSeats, tvTotalPrice, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvShowDateTime = itemView.findViewById(R.id.tv_show_datetime);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            itemView.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        }

        public void bind(BookingHistoryResponse booking) {
            tvBookingCode.setText("ID: " + booking.getBookingId());
            
            // Hiển thị tên phim nếu đã được load
            Movie movie = booking.getMovie();
            if (movie != null) {
                tvMovieTitle.setText(movie.getTitle());
            } else {
                tvMovieTitle.setText("Đang tải tên phim...");
            }
            
            // Hiển thị ngày đặt vé
            if (booking.getBookingDate() != null) {
                String date = booking.getBookingDate().replace("T", " ").substring(0, 16);
                tvShowDateTime.setText("Ngày đặt: " + date);
            }

            if (booking.getSeatNumbers() != null) {
                tvSeats.setText("Ghế: " + String.join(", ", booking.getSeatNumbers()));
            }

            tvTotalPrice.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
            tvStatus.setText(booking.getStatus());

            if ("EXPIRED".equals(booking.getStatus())) {
                tvStatus.setBackgroundResource(android.R.color.darker_gray);
            } else if ("CANCELLED".equals(booking.getStatus())) {
                tvStatus.setBackgroundResource(android.R.color.holo_red_light);
            } else {
                tvStatus.setBackgroundResource(android.R.color.holo_green_light);
            }
        }
    }
}
