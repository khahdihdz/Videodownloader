# Video Downloader

Ứng dụng Android tải video từ các nguồn công khai được hỗ trợ, tập trung vào YouTube và Facebook. Giao diện tiếng Việt, Material 3 và quy trình tải video trực tiếp trên thiết bị.

## Tính năng

- Nhập URL video và tự động nhận diện nền tảng.
- Nhận URL từ Android Share Sheet.
- Chuẩn hóa các dạng URL YouTube: video, Shorts, youtu.be, live.
- Hỗ trợ URL Facebook công khai: video/watch, Reels và một số dạng share.
- Phân tích metadata, tiêu đề, thời lượng và thumbnail khi nguồn cung cấp được dữ liệu.
- Liệt kê chất lượng/format thực tế do yt-dlp phát hiện, không hard-code link tải.
- Tải video bằng yt-dlp-android.
- Hiển thị tiến trình tải trên foreground service và thông báo hệ thống.
- Lưu video vào MediaStore tại `Movies/VideoDownloader`.
- Giao diện Material 3, hỗ trợ sáng/tối.
- GitHub Actions tự động chạy test, lint và build APK debug.

## Kiến trúc

- Kotlin + Jetpack Compose + Material 3.
- MVVM/StateFlow.
- Repository/downloader abstraction.
- Coroutines.
- Foreground Service cho tác vụ tải nền.
- MediaStore cho lưu trữ video.
- yt-dlp-android làm engine tải/phân tích nguồn.

## Build cục bộ

Yêu cầu JDK 17 và Android SDK phù hợp.

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

APK debug sau khi build nằm tại:

```
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

Workflow build thực hiện:

1. Checkout mã nguồn.
2. Thiết lập JDK 17 và Gradle cache.
3. Tạo Gradle Wrapper 8.9 trên runner nếu cần.
4. Chạy unit test.
5. Chạy Android Lint.
6. Build APK debug.
7. Upload APK thành artifact khi các bước kiểm tra thành công.

## Quyền riêng tư và pháp lý

Ứng dụng không yêu cầu lưu mật khẩu, cookie hay thông tin đăng nhập của người dùng. Ứng dụng chỉ hướng tới nội dung công khai và không thực hiện vượt qua DRM, paywall, đăng nhập hoặc quyền riêng tư của nội dung.

Chỉ tải nội dung mà bạn có quyền tải xuống, sử dụng hoặc lưu trữ. Việc tải nội dung có thể chịu điều khoản của từng nền tảng và pháp luật địa phương.

## Giới hạn hiện tại

- Tạm dừng/tiếp tục chưa được cung cấp ở giao diện hiện tại vì engine tải hiện tại chưa được tích hợp cơ chế resume riêng.
- Một số URL công khai có thể không phân tích hoặc tải được khi nền tảng yêu cầu trình duyệt, cookie hoặc thay đổi cơ chế phân phối.
- Facebook chỉ hỗ trợ nội dung công khai; không hỗ trợ bypass nội dung riêng tư hoặc yêu cầu đăng nhập.
- Chất lượng khả dụng phụ thuộc format mà nguồn thực tế trả về.

## Lịch sử thay đổi

Xem [CHANGELOG.md](CHANGELOG.md) để theo dõi các phiên bản và thay đổi chính.

## Giấy phép

MIT License. Xem [LICENSE](LICENSE).
