package com.example.lab10.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.adapters.ChatMessageAdapter;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.AIBookingRequest;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.Booking;
import com.example.lab10.models.ChatMessage;
import com.example.lab10.models.ChatRequest;
import com.example.lab10.models.Movie;
import com.example.lab10.models.PaymentResponse;
import com.example.lab10.models.PaymentStatusResponse;
import com.example.lab10.models.Showtime;
import com.example.lab10.utils.ChatHistoryManager;
import com.example.lab10.utils.SessionManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // Regex để parse [ID: M{movieId}-ST{showtimeDetailId}] từ AI response
    private static final Pattern ACTION_PATTERN =
            Pattern.compile("\\[ID:\\s*M(\\d+)-ST(\\d+)\\]");

    // Regex detect booking confirmation pattern từ AI
    // Ví dụ: [BOOK: ST1-SEATS(A2,A3)] hoặc detect từ context
    private static final Pattern BOOK_PATTERN =
            Pattern.compile("\\[BOOK:\\s*ST(\\d+)-SEATS\\(([^)]+)\\)\\]");

    // Regex để parse tên ghế từ AI text (khi AI trả "ghế A2", "seat A2, B3")
    private static final Pattern SEAT_NAME_PATTERN =
            Pattern.compile("(?:ghế|seat|chỗ)\\s+([A-H]\\d{1,2}(?:\\s*,\\s*[A-H]\\d{1,2})*)", Pattern.CASE_INSENSITIVE);

    private RecyclerView rvMessages;
    private EditText etInput;
    private ImageButton btnSend;
    private ChatMessageAdapter adapter;
    private MovieApiService apiService;
    private MovieApiService chatApiService;
    private SessionManager sessionManager;
    private ChatHistoryManager chatHistory;

    // Payment polling
    private Handler pollHandler;
    private Runnable pollRunnable;
    private boolean isPolling = false;
    private static final int POLL_INTERVAL_MS = 5000; // 5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        chatHistory = ChatHistoryManager.getInstance();
        pollHandler = new Handler(Looper.getMainLooper());

        if (ApiClient.getAuthToken() == null && sessionManager.getToken() != null) {
            ApiClient.setAuthToken(sessionManager.getToken());
        }

        apiService = ApiClient.getApiService();
        chatApiService = createChatApiService();

        Log.d(TAG, "ChatActivity created. Token present: " + (ApiClient.getAuthToken() != null));

        initViews();
        restoreOrShowWelcome();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentPolling();
    }

    private MovieApiService createChatApiService() {
        String token = ApiClient.getAuthToken();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient chatClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Accept", "*/*")
                            .header("Content-Type", "application/json")
                            .header("User-Agent", "Android-App-Client");

                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(builder.build());
                })
                .addInterceptor(logging)
                .build();

        Retrofit chatRetrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(chatClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return chatRetrofit.create(MovieApiService.class);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMessages = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_chat_input);
        btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter();
        adapter.setOnViewSeatsClickListener(this::onViewSeatsClicked);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void restoreOrShowWelcome() {
        if (chatHistory.hasMessages()) {
            for (ChatMessage msg : chatHistory.getMessages()) {
                adapter.addMessage(msg);
            }
            scrollToBottom();
        } else {
            showWelcomeMessage();
        }
    }

    private void showWelcomeMessage() {
        String userName = sessionManager.getFullName();
        if (userName == null || userName.isEmpty()) {
            userName = sessionManager.getUsername();
        }

        String welcome = "Xin chào" + (userName != null ? " " + userName : "") + "! 🎬\n\n" +
                "Mình là trợ lý ảo của MovieTheater. Mình có thể giúp bạn:\n" +
                "• Xem phim đang chiếu hôm nay\n" +
                "• Tìm suất chiếu và lịch chiếu\n" +
                "• Xem ghế trống và đặt vé\n" +
                "• Đặt vé và thanh toán qua QR\n\n" +
                "Hãy hỏi mình bất cứ điều gì! 😊";

        ChatMessage welcomeMsg = new ChatMessage(welcome, false);
        addAndSaveMessage(welcomeMsg);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage userMsg = new ChatMessage(text, true);
        addAndSaveMessage(userMsg);
        etInput.setText("");
        scrollToBottom();

        // Hiện loading indicator (không save vào history)
        adapter.addMessage(ChatMessage.createLoading());
        scrollToBottom();

        ChatRequest request = new ChatRequest(text);
        Log.d(TAG, "Sending chat message: " + text);

        chatApiService.chatWithUser(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                adapter.removeLoading();
                Log.d(TAG, "Chat response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String botResponse = response.body().string();
                        Log.d(TAG, "Bot response length: " + botResponse.length());
                        handleBotResponse(botResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response", e);
                        showError("Lỗi đọc phản hồi từ server");
                    }
                } else {
                    String errorDetail = "";
                    try {
                        if (response.errorBody() != null) {
                            errorDetail = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorDetail);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    if (response.code() == 401 || response.code() == 403) {
                        showError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                    } else {
                        showError("Lỗi " + response.code() + ": " + errorDetail);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                adapter.removeLoading();
                Log.e(TAG, "Chat API failed: " + t.getClass().getSimpleName()
                        + " - " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ============================================================
    // Bot Response Handling
    // ============================================================

    /**
     * Xử lý phản hồi từ AI:
     * 1. Parse [BOOK: ST{id}-SEATS(A2,A3)] → tự động tạo booking + QR
     * 2. Parse [ID: M{movieId}-ST{showtimeDetailId}] → nút xem ghế
     * 3. Tin nhắn text bình thường
     */
    private void handleBotResponse(String response) {
        ChatMessage botMessage = new ChatMessage(response, false);

        // 1. Check for booking command pattern [BOOK: ST{id}-SEATS(A2,A3)]
        Matcher bookMatcher = BOOK_PATTERN.matcher(response);
        if (bookMatcher.find()) {
            try {
                Long stId = Long.parseLong(bookMatcher.group(1));
                String seatsStr = bookMatcher.group(2);
                List<String> seatNames = parseSeatNames(seatsStr);

                addAndSaveMessage(botMessage);
                scrollToBottom();

                // Tự động tạo booking
                handleBookingAction(stId, seatNames);
                return;
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse BOOK pattern", e);
            }
        }

        // 2. Parse [ID: M...-ST...] cho nút xem ghế
        Matcher matcher = ACTION_PATTERN.matcher(response);
        Long lastMovieId = null;
        Long lastShowtimeDetailId = null;

        while (matcher.find()) {
            try {
                lastMovieId = Long.parseLong(matcher.group(1));
                lastShowtimeDetailId = Long.parseLong(matcher.group(2));
            } catch (NumberFormatException e) {
                Log.w(TAG, "Failed to parse action ID", e);
            }
        }

        if (lastMovieId != null && lastShowtimeDetailId != null) {
            botMessage.setMovieId(lastMovieId);
            botMessage.setShowtimeDetailId(lastShowtimeDetailId);

            // Cũng parse seat names từ text (ví dụ: "ghế A2")
            Matcher seatMatcher = SEAT_NAME_PATTERN.matcher(response);
            if (seatMatcher.find()) {
                List<String> seatNames = parseSeatNames(seatMatcher.group(1));
                botMessage.setSeatNames(seatNames);
            }
        }

        addAndSaveMessage(botMessage);
        scrollToBottom();
    }

    /** Parse "A2, A3, B1" thành List */
    private List<String> parseSeatNames(String seatsStr) {
        List<String> seats = new ArrayList<>();
        for (String s : seatsStr.split(",")) {
            String trimmed = s.trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                seats.add(trimmed);
            }
        }
        return seats;
    }

    // ============================================================
    // AI Booking Flow
    // ============================================================

    /**
     * Tạo booking tự động qua AI:
     * Step 1: Convert seat names → seat IDs
     * Step 2: Call POST /api/booking/ai-booking
     * Step 3: Call POST /api/booking/confirm
     * Step 4: Call POST /api/payment/create-vietqr
     * Step 5: Show QR in chat
     * Step 6: Poll payment status
     */
    private void handleBookingAction(Long showtimeDetailId, List<String> seatNames) {
        Log.d(TAG, "Booking action: ST=" + showtimeDetailId + " seats=" + seatNames);
        addAndSaveMessage(new ChatMessage("🔄 Đang tạo booking cho ghế " + seatNames + "...", false));
        scrollToBottom();

        // Step 1: Convert seat names to IDs
        JsonObject convertRequest = new JsonObject();
        convertRequest.addProperty("showtimeDetailId", showtimeDetailId);
        com.google.gson.JsonArray seatArray = new com.google.gson.JsonArray();
        for (String seat : seatNames) {
            seatArray.add(seat);
        }
        convertRequest.add("seatNames", seatArray);

        apiService.convertSeatNames(convertRequest).enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    JsonObject seatMapping = response.body().getResult();
                    List<Long> seatIds = new ArrayList<>();

                    for (Map.Entry<String, JsonElement> entry : seatMapping.entrySet()) {
                        seatIds.add(entry.getValue().getAsLong());
                    }

                    if (seatIds.isEmpty()) {
                        showError("Không tìm thấy ghế " + seatNames + " trong phòng chiếu này.");
                        return;
                    }

                    Log.d(TAG, "Converted seatIds: " + seatIds);
                    // Step 2: Create booking
                    createAIBooking(showtimeDetailId, seatIds, seatNames);
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) {}
                    showError("Không chuyển đổi được tên ghế: " + err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                showError("Lỗi khi chuyển đổi tên ghế: " + t.getMessage());
            }
        });
    }

    /** Step 2: Tạo booking qua AI */
    private void createAIBooking(Long showtimeDetailId, List<Long> seatIds, List<String> seatNames) {
        AIBookingRequest request = new AIBookingRequest(
                showtimeDetailId, seatIds,
                "AI chat booking cho ghế " + seatNames
        );

        apiService.createAIBooking(request).enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String bookingId = null;
                    if (response.body().getResult() != null && response.body().getResult().has("bookingId")) {
                        bookingId = response.body().getResult().get("bookingId").getAsString();
                    }

                    String successMsg = "✅ Đặt vé thành công!" +
                            (bookingId != null ? "\n📋 Mã booking: " + bookingId : "") +
                            "\n🎬 Ghế: " + seatNames +
                            "\n\n⏳ Đang xác nhận booking...";
                    addAndSaveMessage(new ChatMessage(successMsg, false));
                    scrollToBottom();

                    // Step 3: Confirm booking trước khi tạo QR
                    confirmBooking(bookingId);
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) {}
                    showError("Không thể tạo booking: " + err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                showError("Lỗi khi tạo booking: " + t.getMessage());
            }
        });
    }

    /** Step 3: Confirm booking (PENDING → CONFIRMED) trước khi tạo QR */
    private void confirmBooking(String bookingId) {
        apiService.confirmBookingWithParams(null, null, null, null)
                .enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking confirmed successfully");
                    addAndSaveMessage(new ChatMessage("✅ Booking đã được xác nhận!\n⏳ Đang tạo mã QR thanh toán...", false));
                    scrollToBottom();

                    // Step 4: Generate QR payment
                    generateVietQR(bookingId);
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "Confirm booking failed: " + err);
                    showError("Không thể xác nhận booking: " + err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showError("Lỗi khi xác nhận booking: " + t.getMessage());
            }
        });
    }

    /** Step 3: Tạo VietQR payment */
    private void generateVietQR(String bookingId) {
        apiService.createVietQRForChat().enqueue(new Callback<ApiResponse<PaymentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentResponse>> call, Response<ApiResponse<PaymentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    PaymentResponse payment = response.body().getResult();
                    PaymentResponse.QrData qr = payment.getQrData();

                    if (qr != null && qr.getQrDataURL() != null) {
                        // Step 4: Show QR in chat
                        ChatMessage qrMessage = ChatMessage.createQrMessage(
                                qr.getQrDataURL(),
                                qr.getBankName() != null ? qr.getBankName() : "",
                                qr.getBankAccount() != null ? qr.getBankAccount() : "",
                                qr.getAmount() != null ? qr.getAmount() : (payment.getTotalPrice() != null ? String.valueOf(payment.getTotalPrice().longValue()) : ""),
                                qr.getContent() != null ? qr.getContent() : "",
                                bookingId != null ? bookingId : (payment.getBookingId() != null ? payment.getBookingId() : "")
                        );
                        addAndSaveMessage(qrMessage);
                        scrollToBottom();

                        addAndSaveMessage(new ChatMessage(
                                "📱 Quét mã QR bên trên để thanh toán.\n" +
                                "Hệ thống sẽ tự động xác nhận khi thanh toán thành công! 🎉", false));
                        scrollToBottom();

                        // Step 5: Start polling payment status
                        String bid = bookingId != null ? bookingId : payment.getBookingId();
                        startPaymentPolling(bid);
                    } else {
                        showError("Không nhận được mã QR từ server.");
                    }
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) {}
                    showError("Không tạo được mã QR: " + err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentResponse>> call, Throwable t) {
                showError("Lỗi khi tạo QR thanh toán: " + t.getMessage());
            }
        });
    }

    // ============================================================
    // Payment Status Polling
    // ============================================================

    private void startPaymentPolling(String bookingId) {
        if (isPolling) return;
        isPolling = true;
        Log.d(TAG, "Starting payment polling for booking: " + bookingId);

        pollRunnable = new Runnable() {
            int attempts = 0;
            final int MAX_ATTEMPTS = 120; // 10 phút (120 * 5s)

            @Override
            public void run() {
                if (!isPolling || attempts >= MAX_ATTEMPTS) {
                    if (attempts >= MAX_ATTEMPTS) {
                        addAndSaveMessage(new ChatMessage(
                                "⏰ Hết thời gian chờ thanh toán. Vui lòng thử lại.", false));
                        scrollToBottom();
                    }
                    isPolling = false;
                    return;
                }

                attempts++;
                apiService.checkPaymentStatusAuto().enqueue(new Callback<ApiResponse<PaymentStatusResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaymentStatusResponse>> call,
                                           Response<ApiResponse<PaymentStatusResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResult() != null) {
                            PaymentStatusResponse status = response.body().getResult();
                            if (status.isPaid()) {
                                isPolling = false;
                                onPaymentSuccess(bookingId);
                                return;
                            }
                        }
                        // Continue polling
                        if (isPolling) {
                            pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PaymentStatusResponse>> call, Throwable t) {
                        Log.w(TAG, "Payment poll failed: " + t.getMessage());
                        if (isPolling) {
                            pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                        }
                    }
                });
            }
        };

        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPaymentPolling() {
        isPolling = false;
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void onPaymentSuccess(String bookingId) {
        addAndSaveMessage(new ChatMessage(
                "🎉 Thanh toán thành công!\n\n" +
                "📋 Mã booking: " + bookingId + "\n" +
                "✅ Vé đã được xác nhận.\n" +
                "Cảm ơn bạn đã sử dụng dịch vụ MovieTheater! 🍿\n\n" +
                "Bạn có thể xem vé trong mục \"Lịch sử đặt vé\".", false));
        scrollToBottom();
    }

    // ============================================================
    // Helpers
    // ============================================================

    private void showError(String message) {
        ChatMessage errorMsg = new ChatMessage("⚠️ " + message, false);
        addAndSaveMessage(errorMsg);
        scrollToBottom();
    }

    private void addAndSaveMessage(ChatMessage message) {
        adapter.addMessage(message);
        chatHistory.addMessage(message);
    }

    /**
     * Khi user click "Xem ghế trống":
     * 1. Gọi GET /api/showtime-details/{id} để lấy thông tin suất chiếu
     * 2. Gọi GET /api/movies/detail/{id} để lấy thông tin phim
     * 3. Navigate tới SeatSelectionActivity
     */
    private void onViewSeatsClicked(Long movieId, Long showtimeDetailId) {
        Toast.makeText(this, "Đang tải thông tin suất chiếu...", Toast.LENGTH_SHORT).show();

        apiService.getShowtimeDetailById(showtimeDetailId).enqueue(new Callback<ApiResponse<Showtime>>() {
            @Override
            public void onResponse(Call<ApiResponse<Showtime>> call, Response<ApiResponse<Showtime>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    Showtime showtime = response.body().getResult();
                    loadMovieAndNavigate(movieId, showtime);
                } else {
                    Log.e(TAG, "Failed to get showtime detail: " + response.code());
                    Toast.makeText(ChatActivity.this,
                            "Không tải được thông tin suất chiếu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Showtime>> call, Throwable t) {
                Log.e(TAG, "Showtime detail failed: " + t.getMessage());
                Toast.makeText(ChatActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMovieAndNavigate(Long movieId, Showtime showtime) {
        apiService.getMovieById(movieId).enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    Movie movie = response.body().getResult();
                    navigateToSeatSelection(showtime, movie);
                } else {
                    Log.e(TAG, "Failed to get movie: " + response.code());
                    Movie fallbackMovie = new Movie();
                    fallbackMovie.setTitle("Phim #" + movieId);
                    navigateToSeatSelection(showtime, fallbackMovie);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                Log.e(TAG, "Movie detail failed: " + t.getMessage());
                Movie fallbackMovie = new Movie();
                fallbackMovie.setTitle("Phim #" + movieId);
                navigateToSeatSelection(showtime, fallbackMovie);
            }
        });
    }

    private void navigateToSeatSelection(Showtime showtime, Movie movie) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE, movie);
        startActivity(intent);

        ChatMessage navMsg = new ChatMessage(
                "🎬 Đang mở sơ đồ ghế cho bạn. Chọn ghế và đặt vé nhé! 💺", false);
        addAndSaveMessage(navMsg);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.post(() ->
                    rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }
}
