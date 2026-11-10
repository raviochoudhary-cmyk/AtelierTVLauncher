$ErrorActionPreference = "Stop"
$ADB = ".\adb.exe"
if (!(Test-Path $ADB)) { $ADB = "adb" }
$TV = "192.168.1.8:5555"
& $ADB connect $TV
if ($LASTEXITCODE -ne 0) { throw "ADB connection failed" }
& $ADB install -r ".\app-debug.apk"
if ($LASTEXITCODE -ne 0) { throw "APK install failed; stock launcher has not been disabled." }
Write-Host "Installed. Keep stock launchers enabled until Home takeover is verified."
& $ADB shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
