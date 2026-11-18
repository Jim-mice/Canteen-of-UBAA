@echo off
setlocal
cd /d "%~dp0"
if errorlevel 1 exit /b 1

call "%~dp0build.bat"
if errorlevel 1 (
    echo 正式源码编译失败，测试已停止。
    exit /b 1
)

chcp 65001 >nul
where javac >nul 2>&1
if errorlevel 1 exit /b 1
set "BUILD_DIR=%CD%\build"
set "TEST_LIST=%TEMP%\canteen-tests-%RANDOM%-%RANDOM%.txt"
for %%F in ("%CD%\test\*.java") do if exist "%%~fF" call :add_test "%%~fF"
if not exist "%TEST_LIST%" (
    echo 错误：test 目录中没有测试源文件。
    exit /b 1
)

javac --release 17 -encoding UTF-8 -cp "%BUILD_DIR%" -d "%BUILD_DIR%" @"%TEST_LIST%"
set "TEST_COMPILE_RESULT=%ERRORLEVEL%"
del "%TEST_LIST%" >nul 2>&1
if not "%TEST_COMPILE_RESULT%"=="0" (
    echo 测试代码编译失败。
    exit /b %TEST_COMPILE_RESULT%
)

java -cp "%BUILD_DIR%" CanteenConfigurationTest
set "TEST_RESULT=%ERRORLEVEL%"
if not "%TEST_RESULT%"=="0" (
    echo 测试失败。
    exit /b %TEST_RESULT%
)
echo 测试全部通过。
exit /b 0

:add_test
set "TEST_PATH=%~1"
set "TEST_PATH=%TEST_PATH:\=/%"
echo "%TEST_PATH%">>"%TEST_LIST%"
exit /b 0
