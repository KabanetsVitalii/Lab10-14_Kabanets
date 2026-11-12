@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
set "JAVA=C:\vscode\jdk\bin\java.exe"

"%JAVA%" -cp "%~dp0record-system-1.0-SNAPSHOT.jar;%~dp0lib\*" Launcher
if errorlevel 1 (
 echo.
 echo Помилка запуску. Перевірте Java 21, бібліотеки та MySQL.
 pause
 exit /b 1
)
endlocal