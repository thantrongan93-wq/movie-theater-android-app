package com.example.lab10.adapters;

import android.content.Intent;
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
import com.example.lab10.models.BookingHistoryResponse;
import com.example.lab10.models.Movie;
import com.example.lab10.models.Showtime;
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
        private Button btnPay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvShowDateTime = itemView.findViewById(R.id.tv_show_datetime);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnPay = itemView.findViewById(R.id.btn_pay);
            if (itemView.findViewById(R.id.btn_cancel) != null) {
                itemView.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
            }
        }

        public void bind(BookingHistoryResponse booking) {
            tvBookingCode.setText("ID: " + booking.getBookingId());
            
            Movie movie = booking.getMovie();
            if (movie != null) {
                tvMovieTitle.setText(movie.getTitle());
            } else {
                tvMovieTitle.setText("Đang tải tên phim...");
            }
            
            if (booking.getBookingDate() != null) {
                String date = booking.getBookingDate().replace("T", " ").substring(0, 16);
                tvShowDateTime.setText("Ngày đặt: " + date);
            }

            if (booking.getSeatNumbers() != null) {
                tvSeats.setText("Ghế: " + String.join(", ", booking.getSeatNumbers()));
            }

            tvTotalPrice.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
            tvStatus.setText(booking.getStatus());

            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                btnPay.setVisibility(View.VISIBLE);
                tvStatus.setBackgroundResource(android.R.color.holo_orange_light);
                
                btnPay.setOnClickListener(v -> {
                    // Tạo đối tượng Booking giả lập
                    Booking b = new Booking();
                    b.setBookingCode(booking.getBookingId());
                    b.setTotalPrice(booking.getTotalPrice());
                    
                    // Thêm thông tin phim để hiển thị ở trang Payment
                    if (movie != null) {
                        Showtime st = new Showtime();
                        st.setMovie(movie);
                        b.setShowtime(st);
                    }
                    
                    Intent intent = new Intent(itemView.getContext(), PaymentActivity.class);
                    intent.putExtra(PaymentActivity.EXTRA_BOOKING, b);
                    intent.putExtra("AUTO_START_PAYMENT", true);
                    itemView.getContext().startActivity(intent);
                });
            } else {
                btnPay.setVisibility(View.GONE);
                if ("EXPIRED".equalsIgnoreCase(booking.getStatus())) {
                    tvStatus.setBackgroundResource(android.R.color.darker_gray);
                } else if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                    tvStatus.setBackgroundResource(android.R.color.holo_red_light);
                } else {
                    tvStatus.setBackgroundResource(android.R.color.holo_green_light);
                }
            }
        }
    }
}
