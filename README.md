# Atelier TV Launcher (prototype 1.0)

A lightweight native Android TV launcher prototype for Xiaomi MiTV-MOOQ1 / tarzan (Android 10, 32-bit ARM).

## Build without Android Studio

This project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.

1. Create a new **private** GitHub repository in your browser.
2. Upload the contents of this folder to the repository (the `.github/workflows` folder must be included).
3. Open the repository's **Actions** tab and allow workflows if GitHub asks.
4. Run **Build Atelier TV APK** using **Run workflow**, or push to `main`.
5. Open the completed workflow run, scroll to **Artifacts**, and download `AtelierTV-debug-apk`.
6. Extract the downloaded artifact; it contains `app-debug.apk`.

No Android Studio or local Android SDK is needed. GitHub builds the APK in its cloud runner.

## Install over ADB

From `C:\platform-tools`, with the TV connected:

```powershell
.\adb.exe install -r .\app-debug.apk
```

Launch it from the TV's app list first. Test app tiles and remote navigation before selecting it as the default home app. Keep stock launchers installed as recovery options.

## Current prototype features

- Declares HOME/DEFAULT and LEANBACK_LAUNCHER entry points.
- Dark cinematic home screen with featured banner.
- Discovers installed Android TV apps and opens them.
- Prioritises Netflix, Prime Video, YouTube, JioHotstar, Apple TV and VLC when installed.
- Remote-friendly focusable app tiles.
- Settings shortcut and refresh action.
- No network, analytics, ads, background services or firmware changes.

## Limitations

- This is an early prototype; it has not yet been tested on the target television.
- App artwork is represented by simple initials in this version.
- HDMI/Live TV shortcuts are not implemented because vendor-specific input APIs need device testing.
- The launcher does not disable or uninstall any existing package.
