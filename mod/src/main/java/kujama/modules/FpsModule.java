package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import kujama.HudModule;

public class FpsModule extends HudModule {
    private int frames, fps;
    private long last = System.currentTimeMillis();

    public FpsModule() { super("FPS", 4, 18); }

    @Override
    public void render(GuiGraphics g) {
        frames++;
        long now = System.currentTimeMillis();
        if (now - last >= 1000) {
            fps = frames;
            frames = 0;
            last = now;
        }
        text(g, fps + " FPS", 0, 0);
    }
}
