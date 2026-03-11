# Movie Ticket Booking Android App

Ứng dụng đặt vé xem phim Android kết nối với API backend Java và PostgreSQL database.

## Tính năng

- 🎬 Xem danh sách phim đang chiếu
- 📅 Xem lịch chiếu theo phim
- 🎫 Chọn ghế và đặt vé
- 📖 Xem lịch sử đặt vé
- ❌ Hủy vé đã đặt
- 👤 Đăng ký và đăng nhập người dùng

## Cấu trúc dự án

```
app/src/main/java/com/example/lab10/
├── activities/          # Các Activity
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MovieDetailActivity.java
│   ├── SeatSelectionActivity.java
│   ├── BookingConfirmationActivity.java
│   └── MyBookingsActivity.java
├── adapters/           # RecyclerView Adapters
│   ├── MovieAdapter.java
│   ├── ShowtimeAdapter.java
│   ├── SeatAdapter.java
│   └── BookingAdapter.java
├── api/                # API Service & Client
│   ├── ApiClient.java
│   └── MovieApiService.java
├── models/             # Data Models
│   ├── Movie.java
│   ├── Theater.java
│   ├── Showtime.java
│   ├── Seat.java
│   ├── Booking.java
│   ├── User.java
│   └── ...
├── utils/              # Utility Classes
│   ├── SessionManager.java
│   ├── DateTimeUtils.java
│   ├── ImageLoader.java
│   └── CurrencyUtils.java
└── MainActivity.java   # Main Activity - Movie List
```

## Cấu hình

### 1. Cấu hình URL API

Mở file `ApiClient.java` và thay đổi `BASE_URL`:

```java
// Cho Android Emulator kết nối localhost
private static final String BASE_URL = "http://10.0.2.2:8080/";

// Cho thiết bị thật, sử dụng IP máy tính của bạn
// private static final String BASE_URL = "http://192.168.1.100:8080/";

// Hoặc sử dụng URL API đã deploy
// private static final String BASE_URL = "https://your-api-domain.com/";
```

**Lưu ý**: 
- `10.0.2.2` là địa chỉ đặc biệt trong Android Emulator để truy cập `localhost` của máy host
- Với thiết bị thật, cần sử dụng IP thực của máy chạy backend (cùng mạng WiFi)
- Nếu backend đã deploy, sử dụng URL công khai

### 2. API Endpoints cần thiết

Backend của bạn cần cung cấp các endpoints sau:

#### Movies
- `GET /api/movies` - Lấy tất cả phim
- `GET /api/movies/{id}` - Lấy chi tiết phim
- `GET /api/movies/now-showing` - Phim đang chiếu
- `GET /api/movies/coming-soon` - Phim sắp chiếu

#### Theaters
- `GET /api/theaters` - Lấy tất cả rạp
- `GET /api/theaters/{id}` - Lấy chi tiết rạp

#### Showtimes
- `GET /api/showtimes` - Lấy tất cả lịch chiếu
- `GET /api/showtimes/movie/{movieId}` - Lịch chiếu theo phim
- `GET /api/showtimes/{id}` - Chi tiết lịch chiếu

#### Seats
- `GET /api/seats/showtime/{showtimeId}` - Ghế theo lịch chiếu
- `GET /api/seats/available/{showtimeId}` - Ghế còn trống

#### Bookings
- `POST /api/bookings` - Tạo đặt vé mới
- `GET /api/bookings/user/{userId}` - Lịch sử đặt vé
- `GET /api/bookings/{id}` - Chi tiết đặt vé
- `PUT /api/bookings/{id}/cancel` - Hủy vé

#### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký
- `GET /api/users/{id}` - Thông tin người dùng

### 3. Cấu trúc JSON mẫu

#### Movie
```json
{
  "id": 1,
  "title": "Avatar: The Way of Water",
  "description": "Mô tả phim...",
  "director": "James Cameron",
  "duration": 192,
  "genre": "Action, Adventure",
  "releaseDate": "2024-12-16",
  "posterUrl": "https://example.com/poster.jpg",
  "rating": 8.5,
  "language": "English",
  "ageRating": "13+"
}
```

#### Showtime
```json
{
  "id": 1,
  "movieId": 1,
  "movie": {...},
  "theaterId": 1,
  "theater": {...},
  "showDate": "2024-03-15",
  "showTime": "19:30",
  "price": 80000,
  "availableSeats": 50
}
```

#### Seat
```json
{
  "id": 1,
  "theaterId": 1,
  "seatNumber": "5",
  "rowNumber": "A",
  "seatType": "Regular",
  "isAvailable": true,
  "showtimeId": 1
}
```

#### Booking Request
```json
{
  "userId": 1,
  "showtimeId": 1,
  "seatIds": [1, 2, 3]
}
```

#### Login Request
```json
{
  "username": "user123",
  "password": "password123"
}
```

#### API Response
```json
{
  "success": true,
  "message": "Success",
  "data": {...}
}
```

## Cài đặt và chạy

### 1. Yêu cầu
- Android Studio (phiên bản mới nhất)
- Android SDK 24 trở lên
- JDK 11 trở lên
- Backend API đã chạy và có thể truy cập

### 2. Các bước cài đặt

1. **Clone hoặc mở project trong Android Studio**

2. **Sync Gradle**
   - Mở project trong Android Studio
   - Nhấn "Sync Project with Gradle Files"
   - Đợi tải dependencies

3. **Cấu hình API URL**
   - Mở `app/src/main/java/com/example/lab10/api/ApiClient.java`
   - Thay đổi `BASE_URL` theo hướng dẫn ở trên

4. **Build project**
   - Build > Make Project
   - Hoặc nhấn Ctrl+F9 (Windows/Linux) hay Cmd+F9 (Mac)

5. **Chạy ứng dụng**
   - Kết nối thiết bị Android hoặc khởi động Emulator
   - Nhấn Run (hoặc Shift+F10)

### 3. Lưu ý quan trọng

#### Internet Permission
AndroidManifest.xml đã được cấu hình với:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<android:usesCleartextTraffic="true" />
```

#### Network Security (cho Android 9+)
Nếu backend chạy HTTP (không phải HTTPS), đã enable `usesCleartextTraffic="true"` trong manifest.

#### Xử lý lỗi kết nối
- Kiểm tra backend đang chạy
- Kiểm tra URL API đúng
- Với emulator: sử dụng `10.0.2.2` thay vì `localhost`
- Với thiết bị thật: đảm bảo cùng mạng với backend

## Dependencies

Các thư viện chính được sử dụng:

```kotlin
// UI Components
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("com.google.android.material:material:1.13.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// JSON Parsing
implementation("com.google.code.gson:gson:2.10.1")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.15.1")
```

## Sử dụng ứng dụng

### 1. Đăng ký/Đăng nhập
- Mở app lần đầu sẽ hiển thị màn hình đăng nhập
- Nếu chưa có tài khoản, nhấn "Don't have an account? Register"
- Điền thông tin và đăng ký
- Đăng nhập với tài khoản đã tạo

### 2. Xem phim
- Màn hình chính hiển thị danh sách phim
- Nhấn vào phim để xem chi tiết

### 3. Đặt vé
- Trong chi tiết phim, chọn lịch chiếu
- Chọn ghế muốn đặt (ghế trắng = trống, xám = đã đặt, xanh = đã chọn)
- Nhấn "Đặt vé" để xác nhận
- Xem thông tin đặt vé thành công

### 4. Quản lý đặt vé
- Menu > My Bookings để xem lịch sử
- Có thể hủy vé nếu trạng thái là "CONFIRMED"

### 5. Đăng xuất
- Menu > Logout

## Troubleshooting

### Lỗi kết nối API
```
Error: Failed to connect to /10.0.2.2:8080
```
**Giải pháp**:
- Kiểm tra backend đang chạy
- Kiểm tra port đúng (8080 hoặc port khác)
- Với emulator dùng `10.0.2.2`, với device thật dùng IP máy tính

### Lỗi Cleartext Traffic
```
Cleartext HTTP traffic not permitted
```
**Giải pháp**: Đã được cấu hình sẵn trong AndroidManifest với `usesCleartextTraffic="true"`

### Lỗi Retrofit
```
Unable to create converter for class
```
**Giải pháp**: Đảm bảo Gson converter đã được thêm vào dependencies và sync Gradle

### Lỗi Image Loading
Nếu ảnh không hiển thị, kiểm tra:
- URL ảnh trong database có hợp lệ
- Internet permission đã được cấp
- Glide dependency đã được thêm

## Tùy chỉnh

### Thay đổi màu sắc
Sửa file `res/values/colors.xml` để thay đổi theme colors

### Thay đổi định dạng tiền tệ
Mở `utils/CurrencyUtils.java` để thay đổi format tiền

### Thay đổi định dạng ngày/giờ
Mở `utils/DateTimeUtils.java` để tùy chỉnh format

## Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra backend API đang hoạt động
2. Xem log trong Logcat (Android Studio)
3. Kiểm tra network traffic với OkHttp Logging Interceptor
4. Đảm bảo các model class khớp với API response

## License

This project is for educational purposes.
