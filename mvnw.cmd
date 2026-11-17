@echo off
setlocal
set "MAVEN_VERSION=3.9.11"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto run
if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $archive=Join-Path $env:USERPROFILE '.m2\wrapper\apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $archive; Expand-Archive -LiteralPath $archive -DestinationPath (Split-Path '%MAVEN_HOME%') -Force"
if errorlevel 1 exit /b %errorlevel%
:run
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%
