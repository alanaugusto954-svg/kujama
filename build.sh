#!/usr/bin/env bash
# Compila el lanzador. Necesita un JDK 21+ (javac y jar).
set -e
cd "$(dirname "$0")"
rm -rf out dist
mkdir -p out dist/client
javac --release 21 -d out $(find src -name '*.java')
printf 'Main-Class: kujama.launcher.LauncherUI\n' > manifest.txt
jar cfm dist/Kujama-Launcher.jar manifest.txt -C out .
rm -f manifest.txt
cat > dist/jugar.sh <<'EOS'
#!/usr/bin/env bash
cd "$(dirname "$0")"
exec java -jar Kujama-Launcher.jar
EOS
chmod +x dist/jugar.sh
printf '@echo off\r\ncd /d "%%~dp0"\r\nstart "" javaw -jar Kujama-Launcher.jar\r\n' > dist/jugar.bat
echo "Lanzador compilado en launcher/dist"
