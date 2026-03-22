package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.MainActivity;
import com.example.lab10.R;

public class PaymentResultActivity extends AppCompatActivity {

    private ImageView ivStatusIcon;
    private TextView tvStatusTitle;
    private TextView tvStatusMessage;
    private Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        initViews();

        boolean isSuccess = getIntent().getBooleanExtra("IS_SUCCESS", false);
        String bookingId = getIntent().getStringExtra("BOOKING_ID");

        displayResult(isSuccess, bookingId);
    }

    private void initViews() {
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        tvStatusMessage = findViewById(R.id.tv_status_message);
        btnBackHome = findViewById(R.id.btn_back_home);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void displayResult(boolean isSuccess, String bookingId) {
        if (isSuccess) {
            ivStatusIcon.setImageResource(R.drawable.ic_success);
            tvStatusTitle.setText("Thanh toán thành công!");
            tvStatusTitle.setTextColor(getResources().getColor(android.R.color.black));
            tvStatusMessage.setText("Đơn hàng: " + (bookingId != null ? bookingId : "N/A") + "\nCảm ơn bạn đã sử dụng dịch vụ.");
        } else {
            ivStatusIcon.setImageResource(R.drawable.ic_failure);
            tvStatusTitle.setText("Thanh toán thất bại");
            tvStatusTitle.setTextColor(getResources().getColor(android.R.color.black));
            tvStatusMessage.setText("Đã có lỗi xảy ra hoặc giao dịch bị hủy.");
        }
    }
}
