package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import kujama.HudModule;
import net.minecraft.world.item.Items;

public class KeystrokesModule extends HudModule {
    public KeystrokesModule() { super("Keystrokes", Items.STONE_BUTTON, 4, 58); }

    private void key(GuiGraphics g, String label, boolean down, int dx, int dy, int w) {
        int x0 = x + dx, y0 = y + dy;
        g.fill(x0, y0, x0 + w, y0 + 18, down ? 0xB0FFFFFF : 0x80000000);
        int tw = mc.font.width(label);
        g.drawString(mc.font, label, x0 + (w - tw) / 2, y0 + 5, down ? 0xFF000000 : 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics g) {
        key(g, "W", mc.options.keyUp.isDown(), 20, 0, 18);
        key(g, "A", mc.options.keyLeft.isDown(), 0, 20, 18);
        key(g, "S", mc.options.keyDown.isDown(), 20, 20, 18);
        key(g, "D", mc.options.keyRight.isDown(), 40, 20, 18);
        key(g, "SPACE", mc.options.keyJump.isDown(), 0, 40, 58);
    }
}
