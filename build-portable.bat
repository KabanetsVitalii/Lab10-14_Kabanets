@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\vscode\jdk"
set "MVN=C:\vscode\java\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

call "%MVN%" clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/portable/lib
if errorlevel 1 (
 echo Maven не зміг зібрати проєкт.
 pause
 exit /b 1
)
copy /Y "target\record-system-1.0-SNAPSHOT.jar" "target\portable\record-system-1.0-SNAPSHOT.jar" >nul
copy /Y "run.bat" "target\portable\run.bat" >nul
echo.
echo Готово: target\portable\run.bat
pause
endlocal