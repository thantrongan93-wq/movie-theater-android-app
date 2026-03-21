package com.example.lab10.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab10.MainActivity;
import com.example.lab10.R;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";
    private WebView webView;
    private ProgressBar progressBar;
    private View resultLayout;
    private ImageView ivStatusIcon;
    private TextView tvStatusTitle;
    private TextView tvStatusMessage;
    private Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        initViews();

        String paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        Log.d(TAG, "Starting Payment with URL: " + paymentUrl);

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            showError("Không tìm thấy thông tin thanh toán.");
        }
    }

    private void initViews() {
        webView = findViewById(R.id.wv_payment);
        progressBar = findViewById(R.id.pb_payment);
        resultLayout = findViewById(R.id.ll_result_container);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        tvStatusMessage = findViewById(R.id.tv_status_message);
        btnBackHome = findViewById(R.id.btn_back_home);

        // Cấu hình WebView chuyên sâu
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Quan trọng cho VNPay
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Hỗ trợ giao diện web tốt hơn
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // Giả lập User-Agent trình duyệt để tránh bị chặn
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "Navigating to: " + url);

                // Xử lý khi VNPay hoàn tất và gọi về Return URL
                if (url.contains("vnp_ResponseCode=")) {
                    handlePaymentResult(request.getUrl());
                    return true;
                }

                // Cho phép mở URL ngân hàng/ví qua App (nếu có cài đặt)
                if (url.startsWith("http") || url.startsWith("https")) {
                    return false;
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Scheme error: " + e.getMessage());
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    Log.e(TAG, "MainFrame Error: " + error.getDescription());
                }
            }
        });

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void handlePaymentResult(Uri data) {
        webView.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);

        String responseCode = data.getQueryParameter("vnp_ResponseCode");
        String transactionNo = data.getQueryParameter("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            ivStatusIcon.setImageResource(android.R.drawable.checkbox_on_background);
            tvStatusTitle.setText("Thanh toán thành công!");
            tvStatusMessage.setText("Mã giao dịch: " + transactionNo);
        } else {
            ivStatusIcon.setImageResource(android.R.drawable.ic_delete);
            tvStatusTitle.setText("Giao dịch thất bại");
            tvStatusMessage.setText("Mã lỗi: " + responseCode + "\nVui lòng thử lại sau.");
        }
    }

    private void showError(String message) {
        webView.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        tvStatusTitle.setText("Lỗi");
        tvStatusMessage.setText(message);
    }
}
