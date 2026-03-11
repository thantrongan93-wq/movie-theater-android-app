# Hướng dẫn cấu hình nhanh

## Bước 1: Cấu hình URL API

**Tệp cần sửa**: `app/src/main/java/com/example/lab10/api/ApiClient.java`

**Dòng cần thay đổi** (khoảng dòng 16):
```java
private static final String BASE_URL = "http://10.0.2.2:8080/";
```

### Các trường hợp:

#### 1. Chạy trên Android Emulator + Backend ở localhost
```java
private static final String BASE_URL = "http://10.0.2.2:8080/";
```
> `10.0.2.2` là địa chỉ đặc biệt để truy cập localhost của máy host từ emulator

#### 2. Chạy trên thiết bị thật + Backend ở localhost  
Tìm IP máy tính của bạn:
- Windows: Mở CMD, gõ `ipconfig`, tìm IPv4 Address (ví dụ: 192.168.1.100)
- Mac/Linux: Mở Terminal, gõ `ifconfig`, tìm inet (ví dụ: 192.168.1.100)

```java
private static final String BASE_URL = "http://192.168.1.100:8080/";
```
> Thay 192.168.1.100 bằng IP thực của máy bạn
> Đảm bảo điện thoại và máy tính cùng mạng WiFi

#### 3. Backend đã deploy online
```java
private static final String BASE_URL = "https://your-api-domain.com/";
```
> Thay bằng URL API thực tế đã deploy (ví dụ: Heroku, AWS, Railway, etc.)

## Bước 2: Kiểm tra Backend

Trước khi chạy app, đảm bảo backend đang hoạt động:

### Kiểm tra bằng browser hoặc Postman:
```
http://your-base-url/api/movies
```

Nếu trả về danh sách phim (JSON) → Backend OK!

## Bước 3: Build và chạy

1. Mở Android Studio
2. File > Sync Project with Gradle Files
3. Build > Make Project
4. Run > Run 'app'

## Lưu ý quan trọng

### Port mặc định
- Backend Java (Spring Boot) thường chạy port: **8080**
- Nếu backend của bạn dùng port khác, thay đổi trong BASE_URL

### HTTPS vs HTTP
- Nếu API dùng HTTPS → OK
- Nếu API dùng HTTP → Đã cấu hình `usesCleartextTraffic="true"` trong AndroidManifest

### Firewall
Nếu không kết nối được:
- Tắt firewall tạm thời để test
- Hoặc thêm rule cho phép port 8080

## Test nhanh

### 1. Test API từ điện thoại
Mở browser trên điện thoại, truy cập:
```
http://your-ip:8080/api/movies
```
Nếu thấy JSON → Kết nối OK

### 2. Xem log
Trong Android Studio > Logcat, filter "OkHttp" để xem request/response

## Ví dụ đầy đủ

**Backend local (Spring Boot):**
- Chạy trên: `localhost:8080`
- Base path: `/`
- API endpoints: `/api/movies`, `/api/bookings`, etc.

**Cấu hình cho Emulator:**
```java
private static final String BASE_URL = "http://10.0.2.2:8080/";
```

**Cấu hình cho thiết bị thật (IP máy: 192.168.1.105):**
```java
private static final String BASE_URL = "http://192.168.1.105:8080/";
```

## Nếu vẫn gặp lỗi

### Lỗi: "Failed to connect"
1. Kiểm tra backend đang chạy
2. Kiểm tra IP/port đúng
3. Kiểm tra firewall
4. Kiểm tra cùng mạng (với device thật)

### Lỗi: "Unable to resolve host"
1. Kiểm tra URL không có typo
2. Kiểm tra internet permission trong AndroidManifest
3. Kiểm tra kết nối internet của điện thoại/emulator

### Lỗi: "401 Unauthorized" hoặc "403 Forbidden"
1. Kiểm tra API có yêu cầu authentication không
2. Nếu có, cần thêm token/header vào request

---

**Hotline:** Nếu cần hỗ trợ thêm, check log trong Android Studio Logcat!
