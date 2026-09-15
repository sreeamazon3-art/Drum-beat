# Image Beat Maker AI v2 — Android + AI backend

## 1. Backend
cd backend
npm install
copy .env.example to .env
put your AI API key in .env
npm start

## 2. Android API URL
Add this inside `defaultConfig` in app/build.gradle.kts:
buildConfigField("String", "API_BASE_URL", ""http://10.0.2.2:3000"")

For a real phone, replace 10.0.2.2 with the computer's LAN IP and use HTTPS in production.

## 3. Build
Open the project in Android Studio, sync Gradle, then Run.
APK: Build > Generate App Bundles or APKs > Generate APKs.

## Features
- AI reads uploaded handwritten rhythm image
- 64-step kick/snare/hi-hat grid
- L/R sticking preserved separately
- Editable grid
- BPM
- Play
- WAV export directly to Android Downloads

## MP3
For reliable MP3 export, run server-side FFmpeg/LAME conversion or add a licensed Android MP3 encoder. The Android app intentionally exports WAV without requiring a codec library.

## Build APK using only a phone
See `README_MOBILE_BUILD.md`. A GitHub Actions workflow is included at `.github/workflows/build-apk.yml` and builds a debug APK in the cloud.
