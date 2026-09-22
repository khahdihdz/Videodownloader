# Video Downloader

Android video downloader for publicly accessible YouTube and Facebook videos.

## Features
- URL normalization and platform detection
- YouTube, Shorts, Facebook, Reels, Watch and share URL handling
- Android Share Sheet
- Local yt-dlp based metadata and real downloads
- Quality selection
- Foreground download service
- MediaStore output in Movies/VideoDownloader
- Material 3 UI
- GitHub Actions debug/release builds

## Build
./gradlew test
./gradlew lint
./gradlew assembleDebug

## Privacy
No account passwords, cookies or private credentials are stored. Private/authentication-required content is not bypassed.

## Limitations
Pause/resume is not exposed by the current engine. Some public URLs can fail when the platform blocks non-browser requests.

## License
MIT
