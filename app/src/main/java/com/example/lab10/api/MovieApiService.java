package com.example.lab10.api;

import com.example.lab10.models.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface MovieApiService {

    // ===================== AUTH =====================
    @POST("api/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @POST("api/register")
    Call<ApiResponse<User>> register(@Body User user);

    @POST("api/google-login")
    Call<ApiResponse<LoginResponse>> googleLogin(@Body GoogleLoginRequest request);

    @POST("api/facebook-login")
    Call<ApiResponse<LoginResponse>> facebookLogin(@Body FacebookLoginRequest request);

    @POST("api/logout")
    Call<ApiResponse<Object>> logout();

    // ===================== MOVIES =====================
    @GET("api/movies/getAll")
    Call<ApiResponse<PageResponse<Movie>>> getActiveMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/movies/upcomingMovies")
    Call<ApiResponse<PageResponse<Movie>>> getUpcomingMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/movies/comingSoon")
    Call<ApiResponse<PageResponse<Movie>>> getComingSoonMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/movies/detail/{id}")
    Call<ApiResponse<Movie>> getMovieById(@Path("id") Long id);

    /**
     * Tạo phim mới (Admin)
     */
    @POST("api/movies")
    Call<ApiResponse<Movie>> createMovie(@Body Movie movie);

    @PUT("api/movies/{id}")
    Call<ApiResponse<Movie>> updateMovie(@Path("id") Long id, @Body Movie movie);

    @DELETE("api/movies/{id}")
    Call<ApiResponse<Object>> deleteMovie(@Path("id") Long id);

    // ===================== SHOWTIMES =====================
    @GET("api/showtimes")
    Call<ApiResponse<List<Showtime>>> getAllShowtimes();

    @GET("api/showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovie(@Path("movieId") Long movieId);

    @GET("api/movies/showtimes")
    Call<ApiResponse<List<ShowtimeGroup>>> getMovieShowtimes(@Query("movieId") Long movieId);
    // ===================== SHOWTIME DETAILS =====================
    @GET("api/showtime-details")
    Call<ApiResponse<List<Showtime>>> getAllShowtimeDetails();

    @GET("api/showtime-details/date/{date}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByDate(@Path("date") String date);

    @GET("api/showtime-details/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByMovie(@Path("movieId") Long movieId);

    @GET("api/showtime-details/{id}")
    Call<ApiResponse<Showtime>> getShowtimeDetailById(@Path("id") Long showtimeDetailId);

    @GET("api/showtime-details/{id}/seats")
    Call<ApiResponse<SeatResponse>> getSeatsForShowtimeDetail(@Path("id") Long showtimeDetailId);

    @POST("api/showtime-details/{showtimeId}")
    Call<ApiResponse<Showtime>> createShowtimeDetail(@Path("showtimeId") Long showtimeId,@Body ShowtimeDetailRequest request);

    @DELETE("api/showtime-details/{id}")
    Call<ApiResponse<Object>> deleteShowtimeDetail(@Path("id") Long showtimeDetailId);

    // ===================== BOOKING =====================
    @POST("api/booking")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest bookingRequest);

    @DELETE("api/booking/cancel")
    Call<ApiResponse<Object>> cancelPendingBooking();
    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<BookingHistoryResponse>>> getMyBookingHistory();

    /** Lấy mã QR cho vé đã thanh toán */
    @GET("api/booking/generate-qr/{bookingId}")
    Call<ApiResponse<String>> generateBookingQR(@Path("bookingId") String bookingId);

    // ===================== PAYMENT =====================
    @POST("api/payment/create-vnpay-url")
    Call<ApiResponse<PaymentResponse>> createVNPayUrl(@Body PaymentRequest paymentRequest);

    @POST("api/payment/create-vnpay-url")
    Call<ApiResponse<PaymentResponse>> createVNPayUrlAuto();

    @POST("api/payment/create-vietqr")
    Call<ApiResponse<PaymentResponse>> createVietQR(@Body PaymentRequest paymentRequest);

    @POST("api/payment/create-vietqr")
    Call<ApiResponse<PaymentResponse>> createVietQRAuto();

    @GET("api/payment/status")
    Call<ApiResponse<PaymentStatusResponse>> checkPaymentStatus(@Query("bookingId") String bookingId);

    @POST("api/booking/confirm")
    Call<ApiResponse<Booking>> confirmBookingWithParams(
            @Query("phone") String phone,
            @Query("promotionId") Long promotionId,
            @Query("couponCode") String couponCode,
            @Query("pointsToUse") Integer pointsToUse);

    // ===================== FOOD =====================
    @GET("api/foodItems/getAll")
    Call<ApiResponse<List<FoodItem>>> getAllFoodItems();

    @GET("api/foodCombos/getAll")
    Call<ApiResponse<List<FoodCombo>>> getAllFoodCombos();

    @POST("api/booking/food-only")
    Call<ApiResponse<Object>> createFoodOnlyBooking(
            @Query("bookingId") String bookingId,
            @Body FoodOrderRequest request);

    @POST("api/showtimes")
    Call<ApiResponse<Showtime>> createShowtime(@Body ShowtimeRequest request);

    @POST("api/showtimes")
    Call<ApiResponse<Object>> createShowtimeFromManagement(@Body JsonObject request);

    @POST("api/showtime-details/{id}")
    Call<ApiResponse<Object>> createShowtimeDetail(@Path("id") Long showtimeId, @Body JsonObject request);

    @DELETE("api/showtimes/{id}")
    Call<ApiResponse<Object>> deleteShowtime(@Path("id") Long id);

    @POST("api/payment/cash")
    Call<ApiResponse<Object>> payCash(@Query("cashAmount") Double cashAmount);
    // ===================== LOYALTY =====================
    /** Lấy thông tin loyalty (điểm, tier) của user đang đăng nhập */
    @GET("api/loyalty/me")
    Call<ApiResponse<LoyaltyInfo>> getMyLoyalty();

    /** Lấy loyalty theo userId (Admin) */
    @GET("api/loyalty/user/{userId}")
    Call<ApiResponse<LoyaltyInfo>> getLoyaltyByUser(@Path("userId") Long userId);
    // ===================== USER =====================

    // ===================== ADMIN DASHBOARD =====================
    @POST("api/admin/report/revenue")
    Call<ApiResponse<JsonElement>> getAdminRevenueReport(@Body AdminReportRequest request);

    @POST("api/admin/report/orderVolumeOverview")
    Call<ApiResponse<JsonElement>> getAdminOrderVolumeOverview(@Body AdminReportRequest request);

    @GET("api/admin/report/promotion-usage")
    Call<ApiResponse<JsonElement>> getAdminPromotionUsage(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    // ===================== USER & PROFILE =====================
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    @POST("api/users/change-password")
    Call<ApiResponse<Object>> changePassword(@Body JsonObject passwords);

    @GET("api/users")
    Call<ApiResponse<List<User>>> getAllUsers();

    @GET("api/users/{userId}/username")
    Call<ApiResponse<String>> getUsernameById(@Path("userId") Long userId);

    @GET("api/users/getByPhone")
    Call<ApiResponse<User>> getUserByPhone(@Query("phone") String phone);

    // ===================== LOYALTY (USER) =====================
    @GET("api/loyalty/me")
    Call<ApiResponse<LoyaltyResponse>> getMyLoyalty();

    @GET("api/loyalty/point-history")
    Call<ApiResponse<PageResponse<PointHistory>>> getMyPointHistory(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/loyalty/programs/available")
    Call<ApiResponse<PageResponse<LoyaltyProgram>>> getAvailablePrograms(
            @Query("page") Integer page,
            @Query("size") Integer size);

    // ===================== LOYALTY (ADMIN) =====================
    @GET("api/loyalty/programs")
    Call<ApiResponse<PageResponse<LoyaltyProgram>>> getAllLoyaltyPrograms(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/loyalty/admin/{userId}/point-history")
    Call<ApiResponse<PageResponse<PointHistory>>> getUserPointHistory(
            @Path("userId") Long userId,
            @Query("page") Integer page,
            @Query("size") Integer size);

    @POST("api/loyalty/admin/{userId}/adjust-points")
    Call<ApiResponse<Object>> adjustUserPoints(
            @Path("userId") Long userId,
            @Query("points") Integer points,
            @Query("reason") String reason);

    // ===================== LOYALTY TIER =====================
    @GET("api/LoyaltyTier/tier/{tierId}")
    Call<ApiResponse<LoyaltyTier>> getLoyaltyTierById(@Path("tierId") Long tierId);

    @PUT("api/LoyaltyTier/{tierId}")
    Call<ApiResponse<LoyaltyTier>> updateLoyaltyTier(@Path("tierId") Long tierId, @Body LoyaltyTier tier);

    // ===================== EMPLOYEE / SCAN & CHECK-IN =====================
    @POST("api/booking/scan-qr")
    Call<ApiResponse<ScanResponse>> scanQrBooking(@Query("bookingId") String bookingId);

    @GET("api/booking/details/{bookingCode}")
    Call<ApiResponse<ScanResponse>> getBookingDetails(@Path("bookingCode") String bookingCode);

    @POST("api/booking/check-in/{bookingId}")
    Call<ApiResponse<Object>> checkInBooking(@Path("bookingId") String bookingId);

    // ===================== LEGACY / OTHERS =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

}
