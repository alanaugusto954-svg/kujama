#!/usr/bin/env bash
# Compila el mod (Gradle) y el lanzador (javac), y deja todo en launcher/dist
set -e
cd "$(dirname "$0")"
echo "== Mod de Fabric =="
(cd mod && gradle build)
echo "== Lanzador =="
launcher/build.sh
cp mod/build/libs/kujama-mod-*.jar launcher/dist/client/
echo
echo "Listo. Ejecuta: launcher/dist/jugar.sh"
