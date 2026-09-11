@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0test-cors.ps1" %*
