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

    @POST("api/refresh-token")
    Call<ApiResponse<LoginResponse>> refreshToken(@Body LogoutRequest refreshRequest);

    // ===================== MOVIES =====================
    /** Lấy tất cả phim (có phân trang) */
    @GET("api/movies/getAll")
    Call<ApiResponse<PageResponse<Movie>>> getActiveMovies();

    /** Phim đang chiếu (upcoming) - Cập nhật theo API trong ảnh */
    @GET("api/movies/upcomingMovies")
    Call<ApiResponse<PageResponse<Movie>>> getUpcomingMovies(
            @Query("page") Integer page,
            @Query("size") Integer size);

    /** Tìm kiếm phim */
    @GET("api/movies/search")
    Call<ApiResponse<List<Movie>>> searchMovies(
            @Query("keyword") String keyword,
            @Query("type") String type);

    /** Chi tiết phim */
    @GET("api/movies/detail/{id}")
    Call<ApiResponse<Movie>> getMovieById(@Path("id") Long id);

    // ===================== SHOWTIMES =====================
    @GET("api/showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovie(@Path("movieId") Long movieId);

    @GET("api/showtimes/{id}")
    Call<ApiResponse<Showtime>> getShowtimeById(@Path("id") Long id);

    // ===================== SHOWTIME DETAILS =====================
    @GET("api/showtime-details/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByMovie(@Path("movieId") Long movieId);

    @GET("api/showtime-details/{id}/seats")
    Call<ApiResponse<List<Seat>>> getSeatsForShowtimeDetail(@Path("id") Long showtimeDetailId);

    @GET("api/showtime-details/date/{date}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByDate(@Path("date") String date);

    // ===================== BOOKING =====================
    @POST("api/booking")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest bookingRequest);

    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    @GET("api/booking/preview")
    Call<ApiResponse<Booking>> previewBooking();

    @POST("api/booking/confirm")
    Call<ApiResponse<Booking>> confirmBooking(
            @Query("phone") String phone,
            @Query("promotionId") Long promotionId,
            @Query("couponCode") String couponCode);

    // ===================== USER =====================
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    // ===================== CINEMA ROOMS =====================
    @GET("api/cinema-room")
    Call<ApiResponse<List<Room>>> getAllRooms();

    @GET("api/cinema-room/{id}")
    Call<ApiResponse<Room>> getRoomById(@Path("id") Long id);

    // ===================== LEGACY =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

    @POST("api/ticket")
    Call<ApiResponse<Booking>> createTicket(@Body TicketRequest ticketRequest);
}
