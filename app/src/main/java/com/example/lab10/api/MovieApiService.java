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
    Call<ApiResponse<PageResponse<Movie>>> getActiveMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

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

    // ===================== SHOWTIME DETAILS =====================
    @GET("api/showtime-details/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByMovie(@Path("movieId") Long movieId);

        @GET("api/showtime-details/{id}")
        Call<ApiResponse<Showtime>> getShowtimeDetailById(@Path("id") Long showtimeDetailId);

    @GET("api/showtime-details/{id}/seats")
    Call<ApiResponse<List<Seat>>> getSeatsForShowtimeDetail(@Path("id") Long showtimeDetailId);

    // ===================== BOOKING =====================
    @POST("api/booking")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest bookingRequest);

    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<BookingHistoryResponse>>> getMyBookingHistory();

    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    // ===================== USER (PERSONAL) =====================
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    @POST("api/users/change-password")
    Call<ApiResponse<Object>> changePassword(@Body ChangePasswordRequest request);

    // ===================== USER MANAGEMENT (ADMIN) =====================
    @GET("api/admin/users")
    Call<ApiResponse<List<User>>> getAllUsers();

    @DELETE("api/admin/users/{userId}")
    Call<ApiResponse<Object>> deleteUser(@Path("userId") Long userId);

    @PUT("api/admin/users/{userId}/role")
    Call<ApiResponse<User>> updateUserRole(@Path("userId") Long userId, @Query("role") String role);

    // ===================== LOYALTY (USER) =====================
    @GET("api/loyalty/info/{userId}")
    Call<ApiResponse<LoyaltyInfo>> getLoyaltyInfo(@Path("userId") Long userId);

    @GET("api/loyalty/point-history")
    Call<ApiResponse<List<PointHistory>>> getPointHistory(
            @Query("page") int page,
            @Query("size") int size
    );

    // ===================== LOYALTY (ADMIN) =====================
    @POST("api/loyalty/admin/{userId}/adjust-points")
    Call<ApiResponse<LoyaltyInfo>> adjustPoints(
            @Path("userId") Long userId,
            @Body AdjustPointsRequest request
    );

    @GET("api/loyalty/admin/{userId}/point-history")
    Call<ApiResponse<List<PointHistory>>> getAdminPointHistory(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/loyalty/admin/phone")
    Call<ApiResponse<User>> searchUserByPhone(@Query("phone") String phone);

    @GET("api/loyalty/admin/email")
    Call<ApiResponse<User>> searchUserByEmail(@Query("email") String email);

    // ===================== LEGACY / OTHERS =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

}
