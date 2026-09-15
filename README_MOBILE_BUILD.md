# Image Beat Maker AI — Mobile/Cloud APK Build

This copy is prepared so you can build the Android APK using only your phone and GitHub Actions.

## Phone-only method (recommended)

1. Create/sign in to a GitHub account.
2. In GitHub, create a **new repository** (for example `image-beat-maker-ai`).
3. Upload **all files and folders inside this project** to the repository. Keep the `.github/workflows/build-apk.yml` file.
4. Commit the files to the `main` branch.
5. Open the repository → **Actions** → **Build Android APK**.
6. Tap **Run workflow** (or push to `main`, which starts a build automatically).
7. Wait for the workflow to finish with a green check.
8. Open the completed workflow run → **Artifacts** → `image-beat-maker-debug-apk`.
9. Download the ZIP artifact to your phone and extract it. Inside is `app-debug.apk`.
10. Tap the APK and allow your browser/file manager to install apps from that source if Android asks.

## If GitHub does not show Actions

Open the repository's **Actions** tab and enable workflows if GitHub asks. The workflow file must be at:
`.github/workflows/build-apk.yml`

## AI backend

The APK is configured by default for the Android emulator URL `http://10.0.2.2:3000`.
For a real phone, change `API_BASE_URL` in `app/build.gradle.kts` to your deployed HTTPS backend URL before building.

Do NOT put an OpenAI API key in the Android app. Put it only on the backend.

## Backend deployment

The `backend` folder contains the Node.js AI server from the complete project. Deploy it to a Node-compatible HTTPS host, set `OPENAI_API_KEY`, and use its URL in the Android app.

## Local phone testing

If the backend runs on your computer, use your computer's LAN IP instead of `10.0.2.2`, and make sure the phone and computer are on the same Wi-Fi network. For production, use HTTPS.

## What the APK includes

- handwritten rhythm image upload
- AI analyze request to backend
- L/R sticking preserved separately from kick/snare
- 64-step kick/snare/hi-hat grid
- editable steps
- BPM 40–240
- playback
- WAV export to Downloads

## MP3

This build is APK-ready and includes WAV export. Reliable MP3 encoding is best handled server-side with FFmpeg/LAME or a bundled encoder library. The Android app should not depend on a device-specific MP3 encoder.
