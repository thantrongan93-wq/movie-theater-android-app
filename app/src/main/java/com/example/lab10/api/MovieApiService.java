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
    /** Lấy tất cả phim (có phân trang) — data trả về là Page object */
    @GET("api/movies/getAll")
    Call<ApiResponse<PageResponse<Movie>>> getActiveMovies();

    /** Phim đang chiếu (upcoming) */
    @GET("api/movies/upcomingMovies")
    Call<ApiResponse<List<Movie>>> getUpcomingMovies();

    /** Tìm kiếm phim */
    @GET("api/movies/search")
    Call<ApiResponse<List<Movie>>> searchMovies(
            @Query("keyword") String keyword,
            @Query("type") String type);

    /** Chi tiết phim */
    @GET("api/movies/detail/{id}")
    Call<ApiResponse<Movie>> getMovieById(@Path("id") Long id);

    // ===================== SHOWTIMES =====================
    /** Lấy danh sách xuất chiếu theo phim */
    @GET("api/showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovie(@Path("movieId") Long movieId);

    /** Lấy chi tiết xuất chiếu */
    @GET("api/showtimes/{id}")
    Call<ApiResponse<Showtime>> getShowtimeById(@Path("id") Long id);

    // ===================== SHOWTIME DETAILS =====================
    /** Lấy showtime details theo phim (có startTime, price, showtimeDetailId) */
    @GET("api/showtime-details/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByMovie(@Path("movieId") Long movieId);

    /** Lấy tất cả ghế của một showtime detail */
    @GET("api/showtime-details/{id}/seats")
    Call<ApiResponse<List<Seat>>> getSeatsForShowtimeDetail(@Path("id") Long showtimeDetailId);

    /** Lấy showtime details theo ngày */
    @GET("api/showtime-details/date/{date}")
    Call<ApiResponse<List<Showtime>>> getShowtimeDetailsByDate(@Path("date") String date);

    // ===================== BOOKING =====================
    /** Tạo booking mới - không cần userId, server lấy từ JWT */
    @POST("api/booking")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest bookingRequest);

    /** Lấy booking của user hiện tại */
    @GET("api/booking/my-bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    /** Xem trước booking */
    @GET("api/booking/preview")
    Call<ApiResponse<Booking>> previewBooking();

    /** Xác nhận booking (áp dụng khuyến mãi) */
    @POST("api/booking/confirm")
    Call<ApiResponse<Booking>> confirmBooking(
            @Query("phone") String phone,
            @Query("promotionId") Long promotionId,
            @Query("couponCode") String couponCode);

    // ===================== USER =====================
    /** Lấy thông tin user hiện tại */
    @GET("api/users/profile")
    Call<ApiResponse<User>> getMyInfo();

    /** Cập nhật profile */
    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    // ===================== CINEMA ROOMS =====================
    @GET("api/cinema-room")
    Call<ApiResponse<List<Room>>> getAllRooms();

    @GET("api/cinema-room/{id}")
    Call<ApiResponse<Room>> getRoomById(@Path("id") Long id);

    // ===================== LEGACY (giữ lại tương thích) =====================
    @GET("api/seats/room/{roomId}")
    Call<ApiResponse<List<Seat>>> getSeatsByRoom(@Path("roomId") Long roomId);

    @POST("api/ticket")
    Call<ApiResponse<Booking>> createTicket(@Body TicketRequest ticketRequest);
}
