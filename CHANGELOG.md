# CHANGELOG

Các thay đổi đáng chú ý của **Video Downloader**.

## [Chưa phát hành]

### Đã cập nhật

- Việt hóa lại README và bổ sung hướng dẫn build, kiến trúc, quyền riêng tư và giới hạn hiện tại.
- Thêm CHANGELOG để theo dõi lịch sử phát triển của dự án.
- Ghi rõ phạm vi hỗ trợ nội dung công khai và các giới hạn liên quan đến đăng nhập, DRM, paywall và nội dung riêng tư.
- Mô tả quy trình GitHub Actions: test → lint → build APK → upload artifact.

### Kỹ thuật

- Sử dụng yt-dlp-android cho phân tích format và tải video thay vì hard-code URL stream.
- Hỗ trợ nhận diện và chuẩn hóa URL YouTube/Facebook.
- Hỗ trợ Android Share Sheet.
- Tải nền bằng Foreground Service và cập nhật tiến trình qua notification.
- Lưu video thông qua MediaStore tại `Movies/VideoDownloader`.

### Kiểm thử / CI

- Unit test hiện tại chạy thành công trên GitHub Actions.
- Lần chạy CI gần nhất đã thất bại ở bước Android Lint do lỗi tương thích nội bộ của Lint detector `NullSafeMutableLiveData` với Kotlin analysis API khi phân tích `DownloadService.kt`; đây là lỗi của công cụ/dependency, không phải lỗi unit test. Việc build APK sẽ được chạy lại sau khi xử lý CI.

## 0.1.0 — Phiên bản nền tảng

- Khởi tạo ứng dụng Video Downloader.
- Material 3 và Jetpack Compose.
- Nhập URL và nhận URL từ Share Sheet.
- Nhận diện YouTube/Facebook.
- Phân tích metadata và format tải.
- Tải video qua yt-dlp-android.
- Foreground download service.
- Lưu video vào MediaStore.
- GitHub Actions cho kiểm thử và build APK.

## Ghi chú

Dự án chỉ hỗ trợ tải nội dung mà người dùng có quyền tải xuống. Không triển khai cơ chế vượt qua xác thực, DRM, paywall hoặc quyền riêng tư của nội dung.
