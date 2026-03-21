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

    @POST("api/logout")
    Call<ApiResponse<Object>> logout();

    // ===================== MOVIES =====================
    @GET("api/movies/getAll")
    Call<ApiResponse<PageResponse<Movie>>> getActiveMovies();

    /** Phim đang chiếu (upcoming) */
    @GET("api/movies/upcomingMovies")
    Call<ApiResponse<PageResponse<Movie>>> getUpcomingMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    /** Phim sắp chiếu (coming soon) - Theo API mới của bạn */
    @GET("api/movies/comingSoon")
    Call<ApiResponse<PageResponse<Movie>>> getComingSoonMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/movies/detail/{id}")
    Call<ApiResponse<Movie>> getMovieById(@Path("id") Long id);

    // ===================== SHOWTIMES =====================
    @GET("api/showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovie(@Path("movieId") Long movieId);

    @GET("api/movies/showtimes")
    Call<ApiResponse<List<ShowtimeGroup>>> getMovieShowtimes(@Query("movieId") Long movieId);
    // ===================== SHOWTIME DETAILS =====================
    @GET("api/showtime-details/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByMovie(@Path("movieId") Long movieId);

    @GET("api/showtime-details/{id}/seats")
    Call<ApiResponse<SeatResponse>> getSeatsForShowtimeDetail(@Path("id") Long showtimeDetailId);

    // ===================== BOOKING =====================
    @POST("api/booking")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest bookingRequest);

    @DELETE("api/booking/cancel")
    Call<ApiResponse<Object>> cancelPendingBooking();
    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<BookingHistoryResponse>>> getMyBookingHistory();

    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();
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
    // ===================== USER =====================
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    // ===================== LEGACY / OTHERS =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);
}
