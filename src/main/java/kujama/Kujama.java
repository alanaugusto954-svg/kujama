package kujama;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
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
    /** Momento en que entraste al mundo (lo usa la barra superior). */
    public static volatile long sessionStartMs = System.currentTimeMillis();

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ModuleManager.register(new WatermarkModule().desc("Marca Kujama en pantalla."));
        ModuleManager.register(new PillModule().desc("Barra superior: nombre, FPS y tiempo de sesion."));
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

        // ---- Visual: alas con forma real y auras retro
        ModuleManager.register(new ModelWingsModule("Alas rojas", Items.ELYTRA, 0xB3122B, 0x3A0610)
                .desc("Alas de murcielago, fijas al personaje. Miralas con F5."));
        ModuleManager.register(new ModelWingsModule("Alas Kujama", Items.PHANTOM_MEMBRANE, 0x1BD96A, 0x0B3A20)
                .desc("Alas verdes Kujama, fijas al personaje. Miralas con F5."));
        ModuleManager.register(new RetroAuraModule("Aura retro", Items.EXPERIENCE_BOTTLE, 0x1BD96A, 0xFFE14D, 0x2EE6D6)
                .desc("Estilo 8 bits: octogonos de pixeles que suben."));
        ModuleManager.register(new RetroAuraModule("Retro fuego", Items.FIRE_CHARGE, 0xFF3B3B, 0xFF9F1C, 0xFFE14D)
                .desc("Aura de 8 bits en rojo, naranja y amarillo."));
        ModuleManager.register(new RetroAuraModule("Retro neon", Items.GLOWSTONE_DUST, 0xFF4DFF, 0x4DFFFF, 0xFFFFFF)
                .desc("Aura de 8 bits en rosa y celeste neon."));

        // Capa del jugador que dibuja las alas
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
            if (entityRenderer instanceof RenderLayerParent<?, ?> parent && parent.getModel() instanceof PlayerModel) {
                @SuppressWarnings("unchecked")
                RenderLayerParent<AvatarRenderState, PlayerModel> playerParent =
                        (RenderLayerParent<AvatarRenderState, PlayerModel>) parent;
                helper.register(new KujamaWingsLayer(playerParent));
            }
        });

        // ---- Intro cinematica
        ModuleManager.register(new FlagModule("Intro", Items.FILLED_MAP, Category.VISUAL, true)
                .desc("Intro con camara orbitando al entrar al mundo."));
        ModuleManager.register(new FlagModule("Intro 1 vez", Items.REPEATER, Category.VISUAL, true)
                .desc("Mostrar la intro solo una vez por sesion de juego."));
        ModuleManager.register(new FlagModule("Intro de inicio", Items.CLOCK, Category.VISUAL, true)
                .desc("Intro al abrir Minecraft, antes del menu principal."));
        ModuleManager.register(new FlagModule("Menu principal", Items.BOOK, Category.VISUAL, true)
                .desc("Menu principal oscuro de Kujama (apagalo para usar el original)."));

        ModuleManager.load();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(ID, "hud"),
                (graphics, deltaTracker) -> ModuleManager.render(graphics));

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            CpsModule.registerClick();
            return false; // no cancela el ataque
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sessionStartMs = System.currentTimeMillis();
            IntroController.onJoin();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> IntroController.onDisconnect());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WingMotion.tick(client);
            IntroController.tick(client);
            TitleController.tick(client);
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
