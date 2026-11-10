$ErrorActionPreference = "Continue"
$ADB = ".\adb.exe"
if (!(Test-Path $ADB)) { $ADB = "adb" }
$TV = "192.168.1.8:5555"
& $ADB connect $TV
& $ADB shell pm enable com.google.android.tvlauncher
& $ADB shell pm enable com.google.android.leanbacklauncher
& $ADB shell pm enable com.mitv.tvhome.atv
& $ADB shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
& $ADB shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
