@echo off
chcp 65001 >nul
cd /d "%~dp0"

rem Usage: setup-deps.bat [path\to\classes.kxr]
rem Or set GA2_BIN to the game bin folder.

set "KXR=%~1"
if not defined KXR if defined GA2_BIN set "KXR=%GA2_BIN%\classes.kxr"
if not defined KXR if exist "%SystemDrive%\Cyberstep\GetAmped2_TW\bin\classes.kxr" set "KXR=%SystemDrive%\Cyberstep\GetAmped2_TW\bin\classes.kxr"

set OUTJAR=%~dp0lib\classes.jar
set "NATIVE=%GA2_BIN%"
if not defined NATIVE if defined KXR for %%I in ("%KXR%") do set "NATIVE=%%~dpI"
if not defined NATIVE set "NATIVE=%~dp0"

set "JAVA="
if exist "%~dp0jdk\bin\java.exe" set "JAVA=%~dp0jdk\bin\java.exe"
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA (
  where java >nul 2>&1 && for /f "delims=" %%i in ('where java') do set "JAVA=%%i"
)
set "JAVAC="
if exist "%~dp0jdk\bin\javac.exe" set "JAVAC=%~dp0jdk\bin\javac.exe"
if not defined JAVAC if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"

if not defined KXR (
  echo Please pass classes.kxr path, or set GA2_BIN.
  echo Example: setup-deps.bat "D:\Games\GetAmped2_TW\bin\classes.kxr"
  pause
  exit /b 1
)
if not exist "%KXR%" (
  echo Cannot find classes.kxr: %KXR%
  pause
  exit /b 1
)
if not defined JAVA (
  echo Cannot find java. Put JDK in .\jdk or set JAVA_HOME.
  pause
  exit /b 1
)
if not defined JAVAC (
  echo Cannot find javac. Need full JDK.
  pause
  exit /b 1
)

if not exist "%~dp0lib" mkdir "%~dp0lib"
if not exist "%~dp0tools\out" mkdir "%~dp0tools\out"

if not exist "%OUTJAR%" (
  echo Missing bootstrap lib\classes.jar needed to run the extractor.
  echo Place an existing classes.jar into lib\ then re-run, or copy one from a built release.
  pause
  exit /b 1
)

"%JAVAC%" -encoding UTF-8 -cp "%OUTJAR%" -d "%~dp0tools\out" "%~dp0tools\ExtractClassesJar.java"
if errorlevel 1 (
  echo Compile ExtractClassesJar failed
  pause
  exit /b 1
)

"%JAVA%" -cp "%OUTJAR%;%~dp0tools\out" -Djava.library.path="%NATIVE%" ExtractClassesJar "%KXR%" "%OUTJAR%"
if errorlevel 1 (
  echo Extract failed
  pause
  exit /b 1
)

echo.
echo OK: %OUTJAR%
pause
