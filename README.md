# CarLauncher

A lightweight Android launcher designed for phones and Android-based vehicle systems.

## Features
- Lists launchable installed apps in a large touch-friendly grid.
- Displays each app's icon and name.
- Search/filter apps instantly.
- Tap an app to launch it.
- Can be selected as the Android Home/launcher app.
- Automotive hardware is optional, so the APK also installs on ordinary Android phones.
- No third-party runtime libraries.

## Build APK on GitHub
1. Create an empty GitHub repository.
2. Upload this project with the `.github` folder included.
3. Open **Actions** > **Build Android APK** > **Run workflow**.
4. Download the **CarLauncher-APK** artifact.
5. Extract `app-debug.apk` and install it on your phone.

On the phone, Android may ask you to allow installation from the browser/file-manager used to open the APK.

## Set as launcher
Open CarLauncher and tap **Home settings**, then choose CarLauncher as your default Home app.
