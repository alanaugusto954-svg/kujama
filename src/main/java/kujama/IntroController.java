package kujama;

import net.minecraft.client.Minecraft;

/** Decide cuando mostrar la intro: una vez por entrada al mundo (y opcionalmente una vez por sesion). */
public final class IntroController {
    private static boolean pending;
    private static boolean shownThisSession;
    private static int ticksSinceJoin;

    private IntroController() {}

    public static void onJoin() {
        pending = true;
        ticksSinceJoin = 0;
    }

    public static void onDisconnect() {
        pending = false;
    }

    public static void tick(Minecraft mc) {
        if (!pending || mc.player == null || mc.level == null) return;
        ticksSinceJoin++;
        if (mc.screen != null) return;     // todavia carga el terreno, o hay otro menu abierto
        if (ticksSinceJoin < 25) return;   // un instante para que el mundo se vea completo
        pending = false;

        if (!ModuleManager.isOn("Intro")) return;
        if (shownThisSession && ModuleManager.isOn("Intro 1 vez")) return;
        shownThisSession = true;
        mc.setScreen(new IntroScreen());
    }
}
