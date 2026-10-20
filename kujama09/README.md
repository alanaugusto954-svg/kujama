# kujama09

Cliente personalizado para **Minecraft Java Edition 1.21.11** (mod de Fabric, solo cliente): intro
cinematográfica al iniciar el juego y menú principal totalmente rediseñado.

> **Estado de compilación:** este proyecto se escribió sin poder compilarlo (el entorno donde se creó
> no tiene acceso a `maven.fabricmc.net`). No hay JAR incluido. Compilalo en tu PC con los pasos de abajo.
> Las firmas de la API (DrawContext con RenderPipelines, KeyInput, ClickableWidget, etc.) se verificaron contra el Javadoc de Yarn `1.21.11+build.4`; si algún import
> falla en tu entorno, el error de Gradle indica exactamente cuál.

## Por qué estas versiones

- **Minecraft 1.21.11** (la versión que usás), **Java 21**, **Fabric Loader 0.19.5**, **Fabric API 0.141.6+1.21.11**, **Loom 1.18**, **Yarn 1.21.11+build.6**, **Gradle 9.5.1** (wrapper incluido).

## Compilar (IntelliJ IDEA o terminal)

Requisitos: JDK 21 y conexión a internet (la primera vez descarga Minecraft y mappings).

```bash
./gradlew build          # Windows: gradlew.bat build
```

El JAR queda en `build/libs/kujama09-1.0.0.jar` (el `-sources.jar` no se instala).

En IntelliJ IDEA: *File > Open* y elegí la carpeta `kujama09` (importa como proyecto Gradle).
Para probarlo sin instalar nada: ejecutá la tarea Gradle `runClient`.

## Instalar

1. Instalá [Fabric Loader](https://fabricmc.net/use/installer/) para 1.21.11 desde tu launcher habitual.
2. Copiá **Fabric API** (`fabric-api-0.141.6+1.21.11.jar`, de Modrinth o CurseForge) y `kujama09-1.0.0.jar` a la carpeta `mods` de tu instancia.
3. Abrí Minecraft con el perfil de Fabric.

No es un programa aparte ni un `.exe`: es un mod `.jar` que se carga dentro de tu Minecraft.

## Qué hace

**Intro** (4,5 s por defecto, configurable de 2 a 8 s): fondo de pueblo con zoom sutil, nubes, partículas
y destellos, barras cinematográficas, logotipo `kujama09` que aparece escalando, brillo que atraviesa el
texto y fundido a negro hacia el menú. `ESC` la omite.

- Se muestra **una sola vez por inicio del cliente**: el estado vive en memoria (`IntroState`) y se marca al
  entrar a cualquier mundo o servidor, así que no se repite al cambiar de mundo, morir ni volver al menú.
- Empieza cuando Minecraft ya terminó de cargar recursos (espera a que desaparezca su pantalla de carga) y
  corre como una `Screen` normal en el hilo de render, sin bloquear nada.

**Menú principal**: Singleplayer, Multiplayer, Mods (solo si Mod Menu está instalado), Minecraft Settings,
KUJAMA09 Settings y Quit Game; todos abren las pantallas reales de Minecraft. Se conservan los botones de
idioma y accesibilidad, la versión del juego y el aviso de copyright (abre la pantalla oficial de créditos).

**KUJAMA09 Settings**: intro on/off, duración, partículas, sonido de intro (apagado por defecto), sonidos del
menú, intensidad de animaciones (0 = sin animaciones), calidad de efectos (baja/media/alta), "Ver intro" y
"Restaurar valores". Se guarda en `config/kujama09.json`.

## Sobre el personaje del menú

No incluí a Bart Simpson: es un personaje con derechos de autor y no lo dibujo. En su lugar el menú tiene una
**mascota original** (un zorro con sudadera azul y patineta) y un pueblo genérico, con la misma paleta
amarillo/azul/naranja/negro. Si querés cambiarla, reemplazá `assets/kujama09/textures/gui/mascot.png`
(512×512, con transparencia) y `background.png` (1920×1080) por tus propias imágenes; no hay que tocar código
mientras mantengas esas dimensiones.

## Estructura

```
src/main/java/com/kujama09/
  Kujama09Client.java        entrada del mod
  IntroState.java            "una vez por inicio"
  config/KujamaConfig.java   configuración JSON
  anim/                      Easing, Colors, ParticleField
  render/                    Backdrop (fondo/nubes/mascota), LogoRenderer
  widget/KujamaButton.java   botón animado (ButtonWidget real)
  screen/                    IntroScreen, KujamaTitleScreen, KujamaSettingsScreen
  mixin/MinecraftClientMixin reemplaza el TitleScreen vanilla
src/main/resources/          fabric.mod.json, mixins, lang (en/es), texturas
```

## Rendimiento

Animaciones basadas en tiempo real transcurrido; las texturas se cargan una vez por el `TextureManager`;
las partículas son rectángulos sin texturas (16/40/80 según calidad) y se desactivan en los ajustes; con
intensidad 0 todo queda estático. No se promete ningún FPS concreto.
