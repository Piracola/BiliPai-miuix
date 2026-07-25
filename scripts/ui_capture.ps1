param(
    [ValidateSet("light", "dark")]
    [string]$Theme = "light",
    [string]$Serial = "",
    [string]$OutputDirectory = ""
)

$ErrorActionPreference = "Stop"

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($null -eq $adbCommand) {
    throw "adb was not found. Configure the Android SDK and add platform-tools to PATH."
}

$deviceLines = @(
    & $adbCommand.Source devices |
        Select-Object -Skip 1 |
        Where-Object { $_ -match '^([^\s]+)\s+device$' }
)

if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($deviceLines.Count -eq 0) {
        throw "No available Android device was found. Start an emulator or connect an authorized device."
    }
    if ($deviceLines.Count -gt 1) {
        throw "Multiple devices were found. Pass a device serial through -Serial."
    }
    $Serial = ([regex]::Match($deviceLines[0], '^([^\s]+)')).Groups[1].Value
}

$deviceState = & $adbCommand.Source -s $Serial get-state
if ($LASTEXITCODE -ne 0 -or $deviceState.Trim() -ne "device") {
    throw "Device is unavailable: $Serial"
}

$repoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $repoRoot "build\reports\ui-validation"
} elseif (-not [System.IO.Path]::IsPathRooted($OutputDirectory)) {
    $OutputDirectory = Join-Path $repoRoot $OutputDirectory
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$component = "com.android.purebilibili.debug/com.android.purebilibili.debug.UiValidationActivity"
& $adbCommand.Source -s $Serial shell am start -W -n $component --es ui_theme $Theme | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "Unable to start the UI validation activity. Install the Debug app first."
}

Start-Sleep -Milliseconds 800

$remotePath = "/sdcard/bilipai-ui-validation-$Theme.png"
& $adbCommand.Source -s $Serial shell screencap -p $remotePath | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Device screenshot failed."
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputPath = Join-Path $OutputDirectory "ui-validation-$Theme-$timestamp.png"
& $adbCommand.Source -s $Serial pull $remotePath $outputPath | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "Unable to pull the screenshot from the device."
}

& $adbCommand.Source -s $Serial shell rm $remotePath | Out-Null
Write-Host "UI screenshot saved: $outputPath"
