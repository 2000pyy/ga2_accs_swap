@echo off
chcp 65001 >nul
cd /d "%~dp0"

setlocal
set "JAVA="
if exist "%~dp0runtime\bin\java.exe" set "JAVA=%~dp0runtime\bin\java.exe"
if not defined JAVA if exist "%~dp0jdk\bin\java.exe" set "JAVA=%~dp0jdk\bin\java.exe"
if not defined JAVA if exist "%~dp0runtime.zip" (
  echo First run: extracting runtime, please wait...
  if not exist "%~dp0runtime" mkdir "%~dp0runtime"
  tar -xf "%~dp0runtime.zip" -C "%~dp0runtime"
  if exist "%~dp0runtime\bin\java.exe" set "JAVA=%~dp0runtime\bin\java.exe"
)
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA (
  where java >nul 2>&1 && for /f "delims=" %%i in ('where java') do (
    set "JAVA=%%i"
    goto :havejava
  )
)
:havejava

set LIB=%~dp0lib\classes.jar
set OUT=%~dp0bin

set "NATIVE=%GA2_BIN%"
if not defined NATIVE if exist "%SystemDrive%\Cyberstep\GetAmped2_TW\bin" set "NATIVE=%SystemDrive%\Cyberstep\GetAmped2_TW\bin"
if not defined NATIVE set "NATIVE=%~dp0"

if not defined JAVA (
  echo Cannot find Java. Keep runtime.zip in this folder, or install Java.
  pause
  exit /b 1
)
if not exist "%LIB%" (
  echo Missing lib\classes.jar
  pause
  exit /b 1
)
if not exist "%OUT%\accsswap\AccsSwapApp.class" (
  echo Missing prebuilt classes in bin\. Re-download the full package.
  pause
  exit /b 1
)

start "" "%JAVA%" -cp "%LIB%;%OUT%" -Djava.library.path="%NATIVE%" -Dfile.encoding=UTF-8 accsswap.AccsSwapApp
endlocal
