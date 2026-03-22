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

    // ===================== USER =====================
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    // ===================== LEGACY / OTHERS =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

}
