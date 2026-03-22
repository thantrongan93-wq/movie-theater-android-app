package com.example.lab10.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab10.MainActivity;
import com.example.lab10.R;
import com.example.lab10.models.Booking;

public class NotificationHelper {

    private static final String CHANNEL_ID   = "booking_channel";
    private static final String CHANNEL_NAME = "Đặt vé";
    private static final String CHANNEL_DESC = "Thông báo xác nhận đặt vé phim";
    private static final int    NOTIFICATION_ID = 1001;

    /**
     * Gọi một lần khi app khởi động (e.g., trong MainActivity.onCreate)
     * để đăng ký NotificationChannel (bắt buộc cho Android 8+).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Gửi thông báo xác nhận đặt vé thành công.
     *
     * @param context  Context (Activity hoặc Application)
     * @param booking  Đối tượng Booking trả về từ server
     */
    public static void sendBookingConfirmationNotification(Context context, Booking booking) {
        // Android 13+ yêu cầu runtime permission POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Không có quyền, bỏ qua
            }
        }

        String movieTitle = "";
        String showDateTime = "";
        String seats = "";

        if (booking.getShowtime() != null) {
            if (booking.getShowtime().getMovie() != null) {
                movieTitle = booking.getShowtime().getMovie().getTitle();
            }
            String date = booking.getShowtime().getShowDate() != null
                    ? DateTimeUtils.formatDate(booking.getShowtime().getShowDate()) : "";
            String time = booking.getShowtime().getStartTime() != null
                    ? DateTimeUtils.formatTime(booking.getShowtime().getStartTime())
                    : DateTimeUtils.formatTime(booking.getShowtime().getShowTime());
            showDateTime = date + " " + time;
        }

        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < booking.getSeats().size(); i++) {
                if (i > 0) sb.append(", ");
                String row = booking.getSeats().get(i).getRowNumber();
                String num = booking.getSeats().get(i).getSeatNumber();
                sb.append(row != null ? row : "").append(num != null ? num : "");
            }
            seats = sb.toString();
        }

        String bookingCode = booking.getBookingCode() != null ? booking.getBookingCode() : "#" + booking.getId();
        String totalPrice  = CurrencyUtils.formatPrice(booking.getTotalPrice());

        String title = "🎬 Đặt vé thành công!";
        String bigText = "Mã: " + bookingCode
                + "\nPhim: " + movieTitle
                + "\nSuất chiếu: " + showDateTime.trim()
                + "\nGhế: " + seats
                + "\nTổng tiền: " + totalPrice;

        // Tap notification → mở MainActivity
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText("Mã: " + bookingCode + " – " + movieTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
