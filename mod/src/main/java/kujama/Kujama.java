package kujama;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import kujama.modules.*;

public class Kujama implements ClientModInitializer {
    public static final String ID = "kujama";
    public static KeyMapping openGuiKey;
    public static KeyMapping zoomKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ModuleManager.register(new WatermarkModule().desc("Marca Kujama en pantalla."));
        ModuleManager.register(new FpsModule().desc("Muestra tus FPS en pantalla."));
        ModuleManager.register(new CpsModule().desc("Clics por segundo (clic izquierdo)."));
        ModuleManager.register(new CoordsModule().desc("Tus coordenadas X, Y, Z."));
        ModuleManager.register(new KeystrokesModule().desc("Muestra W A S D y espacio."));
        ModuleManager.register(new ArmorModule().desc("Armadura y su durabilidad."));
        ModuleManager.register(new SprintModule().desc("Corre sin mantener la tecla."));
        ModuleManager.register(new ZoomModule().desc("Mantene C para acercar la vista."));
        // ---- Kujama Boost: ajustes de rendimiento con un clic
        ModuleManager.register(new SettingModule<>("Particulas", Items.FIREWORK_STAR,
                Options::particles, Kujama::lastConstant).desc("Particulas al minimo: menos cosas que dibujar."));
        ModuleManager.register(new SettingModule<CloudStatus>("Nubes", Items.SNOWBALL,
                Options::cloudStatus, v -> CloudStatus.OFF).desc("Sin nubes: mas FPS en el cielo."));
        ModuleManager.register(new SettingModule<Integer>("Dist. render", Items.ENDER_EYE,
                Options::renderDistance, v -> Math.min(v, 8)).desc("Distancia de render maxima 8 chunks."));
        ModuleManager.register(new SettingModule<Integer>("Dist. simul.", Items.REDSTONE_TORCH,
                Options::simulationDistance, v -> Math.min(v, 6)).desc("Simulacion a 6 chunks: menos carga de CPU."));
        ModuleManager.register(new SettingModule<Double>("Entidades", Items.ZOMBIE_HEAD,
                Options::entityDistanceScaling, v -> Math.min(v, 0.5)).desc("Dibuja menos entidades lejanas."));
        ModuleManager.register(new SettingModule<Boolean>("Sombras", Items.BLACK_DYE,
                Options::entityShadows, v -> false).desc("Quita las sombras de entidades."));
        ModuleManager.register(new SettingModule<Integer>("Mezcla biomas", Items.GRASS_BLOCK,
                Options::biomeBlendRadius, v -> 0).desc("Sin mezcla de colores entre biomas."));
        ModuleManager.register(new SettingModule<Integer>("FPS libres", Items.REDSTONE,
                Options::framerateLimit, v -> 260).desc("Sin limite de FPS (desactiva el tope)."));

        // ---- Visual: cosmeticos con particulas (solo los ves vos)
        ModuleManager.register(new WingsModule("Alas", Items.ELYTRA, ParticleTypes.END_ROD)
                .desc("Alas de particulas blancas. Miralas con F5."));
        ModuleManager.register(new WingsModule("Alas de fuego", Items.BLAZE_POWDER, ParticleTypes.FLAME)
                .desc("Alas de fuego. Miralas con F5."));
        ModuleManager.register(new AuraModule("Aura de alma", Items.SOUL_TORCH, ParticleTypes.SOUL_FIRE_FLAME, false)
                .desc("Espiral de fuego azul que sube a tu alrededor."));
        ModuleManager.register(new AuraModule("Aura electrica", Items.LIGHTNING_ROD, ParticleTypes.ELECTRIC_SPARK, true)
                .desc("Chispas electricas que salen de tu cuerpo."));

        ModuleManager.load();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(ID, "hud"),
                (graphics, deltaTracker) -> ModuleManager.render(graphics));

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            CpsModule.registerClick();
            return false; // no cancela el ataque
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new KujamaMenuScreen(null));
            }
            ModuleManager.tick();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModuleManager.shutdown());
    }

    /** Devuelve el ultimo valor de un enum (para particulas: el nivel MINIMAL), sin depender del nombre de la clase. */
    private static <E extends Enum<E>> E lastConstant(E value) {
        E[] all = value.getDeclaringClass().getEnumConstants();
        return all[all.length - 1];
    }
}
