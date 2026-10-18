@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
if exist dist rmdir /s /q dist
mkdir out
mkdir dist\client
dir /s /b src\*.java > sources.txt
javac --release 21 -d out @sources.txt || exit /b 1
del sources.txt
echo Main-Class: kujama.launcher.LauncherUI> manifest.txt
jar cfm dist\Kujama-Launcher.jar manifest.txt -C out . || exit /b 1
del manifest.txt
(
echo @echo off
echo cd /d "%%~dp0"
echo start "" javaw -jar Kujama-Launcher.jar
) > dist\jugar.bat
echo Lanzador compilado en launcher\dist
