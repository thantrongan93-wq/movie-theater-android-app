package com.example.lab10.api;

import com.example.lab10.models.*;
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
    @GET("api/showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovie(@Path("movieId") Long movieId);

    @GET("api/movies/showtimes")
    Call<ApiResponse<List<ShowtimeGroup>>> getMovieShowtimes(@Query("movieId") Long movieId);
    // ===================== SHOWTIME DETAILS =====================
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
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    // ===================== LEGACY / OTHERS =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

}
