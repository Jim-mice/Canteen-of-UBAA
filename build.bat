@echo off
setlocal
cd /d "%~dp0"
if errorlevel 1 exit /b 1

chcp 65001 >nul
where javac >nul 2>&1
if errorlevel 1 (
    echo 错误：未找到 javac，请安装并配置 JDK 17 或更高版本。
    exit /b 1
)

set "BUILD_DIR=%CD%\build"
set "SOURCE_LIST=%TEMP%\canteen-sources-%RANDOM%-%RANDOM%.txt"
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if errorlevel 1 exit /b 1
mkdir "%BUILD_DIR%"
if errorlevel 1 exit /b 1

for /r "%CD%\src" %%F in (*.java) do call :add_source "%%~fF"
if not exist "%SOURCE_LIST%" (
    echo 错误：src 目录中没有 Java 源文件。
    exit /b 1
)

javac --release 17 -encoding UTF-8 -d "%BUILD_DIR%" @"%SOURCE_LIST%"
set "COMPILE_RESULT=%ERRORLEVEL%"
del "%SOURCE_LIST%" >nul 2>&1
if not "%COMPILE_RESULT%"=="0" (
    echo 编译失败。
    exit /b %COMPILE_RESULT%
)

echo 编译成功，输出目录：build\
exit /b 0

:add_source
set "SOURCE_PATH=%~1"
set "SOURCE_PATH=%SOURCE_PATH:\=/%"
echo "%SOURCE_PATH%">>"%SOURCE_LIST%"
exit /b 0
