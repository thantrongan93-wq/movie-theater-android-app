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
    
    public BookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
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
            
            String seatNumbers = "";
            if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
                seatNumbers = booking.getSeats().stream()
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
            } else if ("PAID".equalsIgnoreCase(booking.getStatus())) {
                tvStatus.setBackgroundResource(android.R.color.holo_green_light);
            }

            // Click vào toàn bộ item để mở Modal chi tiết & QR
            String finalSeatNumbers = seatNumbers;
            itemView.setOnClickListener(v -> showBookingDetailDialog(booking, finalSeatNumbers));
        }

        private void showBookingDetailDialog(Booking booking, String seatNumbers) {
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

            String code = booking.getBookingCode() != null ? booking.getBookingCode() : String.valueOf(booking.getId());
            tvTitle.setText(booking.getShowtime() != null && booking.getShowtime().getMovie() != null ? 
                    booking.getShowtime().getMovie().getTitle() : "N/A");
            tvId.setText("Mã đơn hàng: " + code);
            
            if (booking.getShowtime() != null) {
                tvTime.setText("Thời gian: " + DateTimeUtils.formatDate(booking.getShowtime().getShowDate()) + 
                        " " + DateTimeUtils.formatTime(booking.getShowtime().getShowTime()));
            }
            tvSeatsInfo.setText("Ghế: " + seatNumbers);

            if ("PAID".equalsIgnoreCase(booking.getStatus())) {
                qrContainer.setVisibility(View.VISIBLE);
                tvHint.setVisibility(View.GONE);
                
                // Load QR
                String fullUrl = ApiClient.BASE_URL + "api/booking/generate-qr/" + code;
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
