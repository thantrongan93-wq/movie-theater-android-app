# Hướng Dẫn Test Flow Đặt Vé

## Tổng Quan
App đặt vé xem phim đã được hoàn thiện với đầy đủ chức năng từ đăng ký, đăng nhập, xem phim, chọn suất chiếu, chọn ghế, đặt vé và xem lịch sử đặt vé.

## API Backend
- **URL**: http://76.13.212.30:6868/
- **Swagger Docs**: http://76.13.212.30:6868/swagger-ui/index.html

## Các Bước Test Flow Đặt Vé

### 1. Build và Chạy App
```bash
# Từ thư mục Lab10
./gradlew clean build

# Hoặc từ Android Studio:
# - Click "Sync Project with Gradle Files"
# - Click "Run" để chạy app trên emulator/device
```

### 2. Đăng Ký Tài Khoản (RegisterActivity)
- Mở app lần đầu
- Click "Đăng ký" ở màn hình login
- Điền đầy đủ thông tin:
  - Tên đăng nhập
  - Email
  - Số điện thoại
  - CMND/CCCD
  - Địa chỉ
  - Mật khẩu
  - Xác nhận mật khẩu
- Click "Đăng ký"

**API Endpoint**: `POST /api/register`

### 3. Đăng Nhập (LoginActivity)
- Nhập username và password
- Click "Đăng nhập"
- JWT token sẽ được lưu tự động

**API Endpoint**: `POST /api/login`

### 4. Xem Danh Sách Phim (MainActivity)
- Sau khi đăng nhập, hiển thị danh sách phim đang chiếu
- Phim được hiển thị dạng grid với poster
- Scroll để xem thêm phim

**API Endpoint**: `GET /api/movies/getAll?page=0&size=20`
- Response: Pagination object với `content`, `totalPages`, etc.

### 5. Xem Chi Tiết Phim (MovieDetailActivity)
- Click vào một phim
- Hiển thị thông tin chi tiết:
  - Poster
  - Tên phim
  - Thể loại
  - Thời lượng
  - Đạo diễn
  - Quốc gia
  - Độ tuổi
  - Mô tả
- Danh sách lịch chiếu hiển thị ở dưới

**API Endpoints**: 
- `GET /api/showtime-details/movie/{movieId}` (ưu tiên)
- `GET /api/showtimes/movie/{movieId}` (fallback)

### 6. Chọn Lịch Chiếu
- Trong màn hình chi tiết phim
- Scroll xuống phần "Lịch chiếu"
- Click vào một suất chiếu để chọn

### 7. Chọn Ghế (SeatSelectionActivity)
- Màn hình hiển thị sơ đồ ghế (grid layout)
- Chú thích màu:
  - **Trắng**: Ghế trống (có thể chọn)
  - **Xanh lá**: Ghế đã chọn
  - **Xám**: Ghế đã được đặt
- Click vào ghế để chọn/bỏ chọn
- Thông tin hiển thị:
  - Phim, ngày giờ chiếu
  - Ghế đã chọn
  - Tổng tiền
- Click "Đặt vé" để tiếp tục

**API Endpoint**: 
- `GET /api/showtime-details/{id}/seats` - Lấy danh sách ghế theo showtime detail

### 8. Đặt Vé
- Sau khi click "Đặt vé"
- App gọi API để tạo booking
- Nếu thành công, chuyển sang màn hình xác nhận

**API Endpoint**: `POST /api/booking`
```json
{
  "showtimeDetailId": 123,
  "seatIds": [1, 2, 3]
}
```
**Note**: `userId` không cần gửi, server tự lấy từ JWT token

### 9. Xác Nhận Đặt Vé (BookingConfirmationActivity)
- Hiển thị thông tin booking:
  - Mã đặt vé
  - Thông tin phim
  - Ngày giờ chiếu
  - Rạp/Phòng
  - Ghế đã đặt
  - Tổng tiền
  - Trạng thái
- 2 nút:
  - "Xem lịch sử đặt vé" → MyBookingsActivity
  - "Về trang chủ" → MainActivity

### 10. Xem Lịch Sử Đặt Vé (MyBookingsActivity)
- Từ MainActivity, click menu → "Lịch sử đặt vé"
- Hiển thị danh sách các booking đã đặt
- Mỗi booking hiển thị:
  - Mã booking
  - Tên phim
  - Ngày giờ chiếu
  - Ghế
  - Tổng tiền
  - Trạng thái

**API Endpoint**: `GET /api/booking/my-bookings`

## Debug & Troubleshooting

### Xem Log
Trong Android Studio, mở Logcat và filter theo các tags:
- `MOVIES` - Để debug danh sách phim
- `SHOWTIMES` - Để debug lịch chiếu
- `SEATS` - Để debug ghế ngồi
- `BOOKING` - Để debug đặt vé
- `BOOKINGS` - Để debug lịch sử đặt vé
- `OkHttp` - Để xem raw HTTP request/response

### Các Lỗi Thường Gặp

#### 1. Không load được movies
```
Expected BEGIN_ARRAY but was BEGIN_OBJECT at $.data
```
✅ **Đã fix**: API trả về pagination object, đã tạo `PageResponse<T>` model

#### 2. Không tìm thấy ghế
- Kiểm tra `showtimeDetailId` có được truyền đúng không
- Xem log để kiểm tra endpoint nào đang được gọi
- API có thể dùng `roomId` thay vì `showtimeDetailId`

#### 3. Booking thất bại
- Kiểm tra JWT token có hợp lệ không (xem log OkHttp)
- Kiểm tra `showtimeDetailId` và `seatIds` có đúng không
- Server có thể reject nếu ghế đã được đặt

#### 4. Không xem được lịch sử
- Kiểm tra user đã đăng nhập chưa
- Kiểm tra JWT token có được restore không (sau restart app)

## Kiến Trúc Code

### Models
- **ApiResponse<T>**: Wrapper cho mọi response, hỗ trợ `statusCode` và `code`
- **PageResponse<T>**: Pagination wrapper cho Spring Boot Page
- **Movie**: Thông tin phim
- **Showtime**: Lịch chiếu (có `showtimeDetailId`)
- **Seat**: Ghế ngồi (trạng thái available/booked)
- **Booking**: Đơn đặt vé
- **BookingRequest**: Request body cho booking (chỉ cần `showtimeDetailId` + `seatIds`)
- **User**: Thông tin user (hỗ trợ `phone`/`phoneNumber` alternates)

### Activities
- **LoginActivity**: Đăng nhập
- **RegisterActivity**: Đăng ký
- **MainActivity**: Danh sách phim
- **MovieDetailActivity**: Chi tiết phim + lịch chiếu
- **SeatSelectionActivity**: Chọn ghế + đặt vé
- **BookingConfirmationActivity**: Xác nhận đặt vé thành công
- **MyBookingsActivity**: Lịch sử đặt vé

### Adapters
- **MovieAdapter**: Hiển thị grid phim
- **ShowtimeAdapter**: Hiển thị list lịch chiếu
- **SeatAdapter**: Hiển thị grid ghế
- **BookingAdapter**: Hiển thị list bookings

### API Service
- **MovieApiService**: Interface chứa tất cả endpoints
- **ApiClient**: Retrofit client với logging + JWT interceptor

### Utils
- **SessionManager**: Lưu trữ token, user info trong SharedPreferences
- **CurrencyUtils**: Format tiền VNĐ
- **DateTimeUtils**: Format ngày giờ
- **ImageLoader**: Load ảnh với Glide

## Checklist Hoàn Thành

- [x] Login với JWT authentication
- [x] Register account với confirmPassword
- [x] Browse movies với pagination support
- [x] View movie details
- [x] Load showtimes với showtime-details + fallback
- [x] Select seats từ showtime-detail
- [x] Create booking với showtimeDetailId
- [x] Booking confirmation
- [x] View booking history (my bookings)
- [x] Error handling & logging
- [x] UI layouts hoàn chỉnh

## Next Steps (Tùy Chọn)

- [ ] Cancel booking functionality
- [ ] Booking preview trước khi confirm
- [ ] Payment integration
- [ ] Push notifications
- [ ] QR code cho booking
- [ ] Search & filter movies
- [ ] Movie ratings & reviews
