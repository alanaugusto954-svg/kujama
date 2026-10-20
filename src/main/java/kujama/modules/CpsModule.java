package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import kujama.HudModule;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;

public class CpsModule extends HudModule {
    private static final ArrayDeque<Long> CLICKS = new ArrayDeque<>();

    public CpsModule() { super("CPS", Items.IRON_SWORD, 4, 30); }

    /** Se llama desde el evento de ataque (clic izquierdo). */
    public static void registerClick() {
        prune();
        CLICKS.addLast(System.currentTimeMillis());
    }

    private static void prune() {
        long now = System.currentTimeMillis();
        while (!CLICKS.isEmpty() && now - CLICKS.peekFirst() > 1000) CLICKS.pollFirst();
    }

    @Override
    public void render(GuiGraphics g) {
        prune();
        text(g, CLICKS.size() + " CPS", 0, 0);
    }
}
