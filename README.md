# Atelier TV Launcher — Xiaomi MiTV-MOOQ1 / tarzan

A lightweight Android 10 / 32-bit ARM native launcher. It uses installed app icons and launch intents rather than shipping streaming-service artwork.

## Home screen
- Black, Apple TV-inspired visual language.
- Streaming apps are the first and primary row: Netflix, Prime Video, Apple TV, YouTube, JioHotstar and Airtel Xstream (only apps actually installed appear).
- HDMI 1/2/3, Live TV and AV are shown in a separate inputs row.
- Wi-Fi shortcut and Android Settings shortcut.
- D-pad focus states and click-to-launch.

## Important device-specific limitation
The Xiaomi MiTV-MOOQ1 uses a MediaTek/MStar TV input stack. The Android public API does not guarantee third-party apps can switch HDMI inputs on this firmware. Live TV launches the installed MediaTek Live TV app. HDMI/AV cards open TV input settings; direct source switching may require Xiaomi's private vendor API. Do not assume HDMI switching works until tested on the TV.

## Build without Android Studio
1. Upload the project contents to the root of a GitHub repository, including `.github/workflows/build-apk.yml`.
2. Open Actions and run **Build Atelier TV APK**.
3. Download artifact `AtelierTV-debug-apk`; extract `app-debug.apk`.

## Install
Copy `app-debug.apk` beside `adb.exe` in `C:\platform-tools`, then run:
```powershell
.db.exe connect 192.168.1.8:5555
.db.exe install -r .pp-debug.apk
```
This does not disable or replace the stock launcher automatically.

## Set as Home — only after confirming install and launch
```powershell
.db.exe shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```
If Atelier appears, set it:
```powershell
.db.exe shell cmd package set-home-activity com.ravi.atelier/.MainActivity
.db.exe shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```
Confirm Home opens Atelier and remote navigation works. Keep at least one stock launcher enabled until this is verified. The firmware may ignore `set-home-activity`; verify the resumed activity rather than trusting the command output.

## Restore stock Home
Run `scripts/restore-stock-home.ps1` from PowerShell in the folder containing `adb.exe`, or execute:
```powershell
.db.exe shell pm enable com.google.android.tvlauncher
.db.exe shell pm enable com.google.android.leanbacklauncher
.db.exe shell pm enable com.mitv.tvhome.atv
.db.exe shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
```

## What this build deliberately does not do
- Does not disable Xiaomi/Google packages.
- Does not alter firmware, boot, recovery, vbmeta or partitions.
- Does not claim full Apple tvOS parity.
- Does not claim HDMI switching works through a public API on this vendor firmware.
- Does not change update settings or Chromecast/Assistant services.

## Device facts used
MiTV-MOOQ1 / tarzan, Android 10 (SDK 29), ARM 32-bit. Known installed packages from the supplied package inventory: Netflix `com.netflix.ninja`, Prime Video `com.amazon.amazonvideo.livingroom`, Apple TV `com.apple.atve.androidtv.appletv`, YouTube `com.google.android.youtube.tv`, JioHotstar `in.startv.hotstar`, Airtel Xstream `tv.airtel.xstream.tvapp`, Live TV `com.mediatek.wwtv.tvcenter`.
