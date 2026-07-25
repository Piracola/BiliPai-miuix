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
$shortComponent = "com.android.purebilibili.debug/.debug.UiValidationActivity"
$startOutput = & $adbCommand.Source -s $Serial shell am start -S -W -n $component --es ui_theme $Theme 2>&1
$startExitCode = $LASTEXITCODE
$startOutput | Out-Host
if ($startExitCode -ne 0 -or ($startOutput -join "`n") -match 'Error:') {
    throw "Unable to start the UI validation activity. Install the Debug app first."
}

$activityReady = $false
$activityDeadline = [DateTime]::UtcNow.AddSeconds(10)
do {
    $activityDump = & $adbCommand.Source -s $Serial shell dumpsys activity activities 2>&1
    if ($LASTEXITCODE -eq 0) {
        $resumedLines = $activityDump | Where-Object {
            $_ -match 'mResumedActivity|topResumedActivity'
        }
        $resumedText = $resumedLines -join "`n"
        if ($resumedText.Contains($component) -or $resumedText.Contains($shortComponent)) {
            $activityReady = $true
            break
        }
    }
    Start-Sleep -Milliseconds 200
} while ([DateTime]::UtcNow -lt $activityDeadline)

if (-not $activityReady) {
    throw "UI validation activity did not become the resumed activity within 10 seconds."
}
Start-Sleep -Milliseconds 500

$captureId = [Guid]::NewGuid().ToString("N")
$remotePath = "/sdcard/bilipai-ui-validation-$Theme-$captureId.png"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss-fff"
$safeSerial = $Serial -replace '[^A-Za-z0-9._-]', '_'
$outputPath = Join-Path $OutputDirectory "ui-validation-$safeSerial-$Theme-$timestamp.png"

try {
    & $adbCommand.Source -s $Serial shell screencap -p $remotePath | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Device screenshot failed."
    }

    & $adbCommand.Source -s $Serial pull $remotePath $outputPath | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to pull the screenshot from the device."
    }

    if (-not (Test-Path -LiteralPath $outputPath -PathType Leaf)) {
        throw "Screenshot output was not created: $outputPath"
    }
    $pngBytes = [System.IO.File]::ReadAllBytes($outputPath)
    $pngSignature = [byte[]](137, 80, 78, 71, 13, 10, 26, 10)
    if ($pngBytes.Length -lt $pngSignature.Length) {
        throw "Screenshot output is empty or truncated: $outputPath"
    }
    for ($index = 0; $index -lt $pngSignature.Length; $index++) {
        if ($pngBytes[$index] -ne $pngSignature[$index]) {
            throw "Screenshot output is not a valid PNG file: $outputPath"
        }
    }
} finally {
    & $adbCommand.Source -s $Serial shell rm -f $remotePath 2>$null | Out-Null
}

Write-Host "UI screenshot saved: $outputPath"
