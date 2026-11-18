@echo off
setlocal
cd /d "%~dp0"
if errorlevel 1 exit /b 1

call "%~dp0build.bat"
if errorlevel 1 (
    echo 编译失败，已停止运行。
    exit /b 1
)

java -cp "%~dp0build" CanteenApplication
exit /b %ERRORLEVEL%
