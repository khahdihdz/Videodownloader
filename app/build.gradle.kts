plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
android {
    namespace = "com.khahdihdz.videodownloader"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.khahdihdz.videodownloader"
        minSdk = 24
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0." + buildNumber
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }\n    lint {\n        // Work around an AndroidX Lifecycle lint detector crash caused by an incompatible Kotlin Analysis API.\n        disable += "NullSafeMutableLiveData"\n    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("dev.ffmpegkit-maintained:yt-dlp-android:2.0.2")
    implementation("dev.ffmpegkit-maintained:yt-dlp-android-compat:2.0.2")
    testImplementation("junit:junit:4.13.2")
}
