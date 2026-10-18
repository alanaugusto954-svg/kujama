@echo off
cd /d "%~dp0"
echo == Mod de Fabric ==
pushd mod
call gradle build || exit /b 1
popd
echo == Lanzador ==
call launcher\build.bat || exit /b 1
copy /y mod\build\libs\kujama-mod-*.jar launcher\dist\client\ >nul
echo.
echo Listo. Ejecuta launcher\dist\jugar.bat
