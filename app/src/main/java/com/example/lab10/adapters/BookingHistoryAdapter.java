package com.example.lab10.adapters;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.lab10.R;
import com.example.lab10.activities.PaymentActivity;
import com.example.lab10.api.ApiClient;
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
            String bookingId = booking.getBookingId();
            tvBookingCode.setText("ID: " + bookingId);
            
            Movie movie = booking.getMovie();
            if (movie != null) {
                tvMovieTitle.setText(movie.getTitle());
            }
            
            if (booking.getBookingDate() != null) {
                String date = booking.getBookingDate().replace("T", " ").substring(0, 16);
                tvShowDateTime.setText("Ngày đặt: " + date);
            }

            String seats = "";
            if (booking.getSeatNumbers() != null) {
                seats = String.join(", ", booking.getSeatNumbers());
                tvSeats.setText("Ghế: " + seats);
            }

            tvTotalPrice.setText(CurrencyUtils.formatPrice(booking.getTotalPrice()));
            tvStatus.setText(booking.getStatus());

            btnPay.setVisibility(View.GONE);
            if ("PAID".equalsIgnoreCase(booking.getStatus())) {
                tvStatus.setBackgroundResource(android.R.color.holo_green_light);
            } else if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                btnPay.setVisibility(View.VISIBLE);
                tvStatus.setBackgroundResource(android.R.color.holo_orange_light);
                
                btnPay.setOnClickListener(v -> {
                    Booking b = new Booking();
                    b.setBookingCode(bookingId);
                    b.setTotalPrice(booking.getTotalPrice());
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
            }

            // Click để mở Modal
            String finalSeats = seats;
            itemView.setOnClickListener(v -> showDetailDialog(booking, finalSeats));
        }

        private void showDetailDialog(BookingHistoryResponse booking, String seats) {
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_booking_detail);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView tvTitle = dialog.findViewById(R.id.dialog_movie_title);
            TextView tvId = dialog.findViewById(R.id.dialog_booking_id);
            TextView tvTime = dialog.findViewById(R.id.dialog_show_time);
            TextView tvSeatsInfo = dialog.findViewById(R.id.dialog_seats);
            ImageView ivQr = dialog.findViewById(R.id.dialog_iv_qr);
            LinearLayout qrContainer = dialog.findViewById(R.id.dialog_qr_container);
            TextView tvHint = dialog.findViewById(R.id.dialog_status_hint);
            Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

            tvTitle.setText(booking.getMovie() != null ? booking.getMovie().getTitle() : "N/A");
            tvId.setText("Mã đơn hàng: " + booking.getBookingId());
            tvTime.setText("Ngày đặt: " + booking.getBookingDate().replace("T", " ").substring(0, 16));
            tvSeatsInfo.setText("Ghế: " + seats);

            if ("PAID".equalsIgnoreCase(booking.getStatus())) {
                qrContainer.setVisibility(View.VISIBLE);
                tvHint.setVisibility(View.GONE);
                
                // Load QR
                String fullUrl = ApiClient.BASE_URL + "api/booking/generate-qr/" + booking.getBookingId();
                String token = ApiClient.getAuthToken();
                GlideUrl glideUrl = new GlideUrl(fullUrl, new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build());

                Glide.with(itemView.getContext()).load(glideUrl).into(ivQr);
            } else {
                qrContainer.setVisibility(View.GONE);
                tvHint.setVisibility(View.VISIBLE);
                tvHint.setText("Trạng thái: " + booking.getStatus() + ". Vui lòng thanh toán để lấy mã QR.");
            }

            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }
}
