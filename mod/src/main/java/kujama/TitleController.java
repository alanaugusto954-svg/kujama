package kujama;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Cuando Minecraft muestra su menu principal, lo cambia por el de Kujama (y la primera vez, antes, la intro de inicio).
 * Si algo falla, o desactivas "Menu principal", queda el menu original: el juego siempre se puede usar.
 */
public final class TitleController {
    /** Menu original abierto a proposito (boton "Menu original"): no se reemplaza mientras este abierto. */
    static TitleScreen vanilla;
    private static boolean startupDone;

    private TitleController() {}

    public static void tick(Minecraft mc) {
        if (!(mc.screen instanceof TitleScreen ts) || ts == vanilla || Safe.disabled("title")) return;
        boolean startup = !startupDone && ModuleManager.isOn("Intro de inicio");
        startupDone = true;
        if (startup) {
            mc.setScreen(new StartupScreen());
        } else if (ModuleManager.isOn("Menu principal")) {
            mc.setScreen(new KujamaTitleScreen());
        } else {
            vanilla = ts;
        }
    }

    /** Abre el menu principal que corresponda (el de Kujama o el original). */
    public static void openTitle(Minecraft mc) {
        if (ModuleManager.isOn("Menu principal") && !Safe.disabled("title-ui")) {
            mc.setScreen(new KujamaTitleScreen());
        } else {
            openVanilla(mc);
        }
    }

    public static void openVanilla(Minecraft mc) {
        vanilla = new TitleScreen();
        mc.setScreen(vanilla);
    }
}
