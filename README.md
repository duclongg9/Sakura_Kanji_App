# Sakura_Kanji_App

## Thiết lập cơ sở dữ liệu

1. Cài đặt MySQL 8 và tạo người dùng có quyền tạo cơ sở dữ liệu.
2. Chạy script [`kanji_app_v1.sql`](kanji_app_v1.sql) để khởi tạo toàn bộ bảng.
   Script này đã bao gồm bảng `Role` (với các role `ADMIN`, `USER`, `VIP`) và bảng `User` với các cột cần thiết cho đăng nhập Google như `email`, `imgUrl`, `matKhau` (cho đăng nhập truyền thống) và `roleId`.
3. Xác nhận rằng bảng `User` có cột `imgUrl` và `roleId` không rỗng; khóa ngoại `roleId` phải trỏ tới `Role.id = 2` cho người dùng Google.

Nếu đã tạo DB từ version cũ, hãy chạy lại script trên hoặc tự thêm các cột thiếu bằng câu lệnh:

```sql
ALTER TABLE `User`
  ADD COLUMN IF NOT EXISTS imgUrl VARCHAR(255) NULL,
  MODIFY COLUMN roleId TINYINT NOT NULL DEFAULT 2;
```

## Cấu hình đăng nhập Google

* Ở Android app, cập nhật `strings.xml` (`web_client_id`) bằng OAuth 2.0 Web Client ID lấy từ Google Cloud Console.
* Ở backend (module `kanji-auth-backend`), cấu hình cùng giá trị client ID thông qua:
  * Tham số JVM: `-DKANJI_APP_GOOGLE_CLIENT=<client-id>`; **hoặc**
  * Biến môi trường: `KANJI_APP_GOOGLE_CLIENT=<client-id>`.

Không thiết lập biến trên, backend sẽ dùng giá trị mặc định được cấp sẵn cho môi trường phát triển (trùng với client ID trong repo).

Sau khi đồng bộ client ID và đảm bảo bảng `User` tồn tại, API `/api/auth/google` sẽ tự lưu (hoặc tái sử dụng) người dùng Google và trả về token demo (`demo-<userId>`).
