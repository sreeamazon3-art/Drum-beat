# Image Beat Maker AI — GitHub Mobile APK Build (V4)

This version is prepared for building from a phone using GitHub Actions.

## Repository root must contain

.github/
app/
backend/
build.gradle.kts
settings.gradle.kts
gradle.properties

## Build from phone

1. Create/open your GitHub repository.
2. Upload the CONTENTS of this folder to the repository root.
3. Commit to `main`.
4. Open **Actions**.
5. Select **Build Android APK**.
6. Tap **Run workflow**.
7. Wait for the build to finish.
8. Open the completed run.
9. Under **Artifacts**, download `image-beat-maker-debug-apk`.
10. Extract the downloaded artifact and install `app-debug.apk`.

## Why this version is different

The workflow installs Gradle itself, so the repository does NOT depend on a missing `gradlew` file.

It also uses `actions/setup-java@v5`.

If Actions is disabled, enable it under:
Repository -> Settings -> Actions -> General.
