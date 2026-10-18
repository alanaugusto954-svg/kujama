package kujama.modules;

import kujama.Module;

/** Sprint automatico: corres siempre que avanzas y no estas agachado. */
public class SprintModule extends Module {
    public SprintModule() { super("Sprint automatico", true); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) return;
        if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown()) {
            mc.options.keySprint.setDown(true);
        }
    }
}
