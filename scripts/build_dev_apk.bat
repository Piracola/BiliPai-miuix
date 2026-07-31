@echo off
setlocal

rem Build a signed dev APK while keeping Gradle's verbose output out of the terminal.
set "ROOT_DIR=%~dp0.."
set "LOG_DIR=%ROOT_DIR%\build"
set "LOG_FILE=%LOG_DIR%\assemble-dev.log"
set "BUILD_RESULT=1"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

pushd "%ROOT_DIR%" || (
    echo [ERROR] Cannot open project directory: %ROOT_DIR%
    goto :end
)

echo Building dev APK. Full Gradle output: %LOG_FILE%
rem Disable KSP incremental caches to avoid a known local cache-close failure.
call gradlew.bat :app:assembleDev -Pksp.incremental=false --no-daemon --no-configuration-cache --console=plain > "%LOG_FILE%" 2>&1
set "BUILD_RESULT=%ERRORLEVEL%"

if not "%BUILD_RESULT%"=="0" (
    echo [FAILED] Dev APK build failed. Check: %LOG_FILE%
    popd
    goto :end
)

echo [SUCCESS] Dev APK build completed.
for %%F in ("app\build\outputs\apk\dev\*.apk") do echo APK: %CD%\%%F
echo Log: %LOG_FILE%
popd

:end
if /I not "%~1"=="--no-pause" pause
exit /b %BUILD_RESULT%
