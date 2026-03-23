@echo off
REM Minimal gradlew.bat that delegates to installed gradle or wrapper if present
SET DIR=%~dp0
IF EXIST "%DIR%\gradle\wrapper\gradle-wrapper.jar" (
  java -jar "%DIR%\gradle\wrapper\gradle-wrapper.jar" %*
) ELSE (
  gradle %*
)
