# Kujama Client (Minecraft 1.21.11, Fabric)

- `mod/`: mod de cliente (marca Kujama, HUD de FPS/CPS/coordenadas/keystrokes/armadura, zoom, sprint automatico, menu).
- `launcher/`: lanzador con interfaz propia. Descarga Minecraft 1.21.11, Fabric y los mods de optimizacion
  (Sodium renombrado a "Kujama Renderer", Lithium, FerriteCore, ModernFix, EntityCulling, ImmediatelyFast, Fabric API).

## Compilar el mod sin instalar nada (GitHub)
1. Crea un repositorio en GitHub y sube TODO el contenido de esta carpeta (incluida `.github`).
2. Pestana **Actions** > "Compilar mod Kujama" > cuando termine, descarga el artefacto `kujama-mod`.
3. Pone el `kujama-mod-1.0.0.jar` dentro de la carpeta `client` del lanzador. Listo.

## Compilar en tu PC
Necesitas JDK 21 y Gradle 9.x: `build-all.bat` (Windows) o `./build-all.sh`.
Si falla por versiones, copia `loader_version` y `fabric_version` desde https://fabricmc.net/develop a `mod/gradle.properties`.

## Controles
Shift derecho: menu de modulos. C (mantener): zoom.

## Licencias
Sodium, Lithium, FerriteCore, ModernFix, EntityCulling e ImmediatelyFast se descargan de Modrinth con sus licencias
originales (Sodium: LGPL-3.0). Kujama solo cambia el nombre visible de Sodium; el credito y la licencia siguen dentro del jar.
