package kujama.modules;

import kujama.Category;
import kujama.Module;
import net.minecraft.world.item.Items;

/** Sprint automatico: corres siempre que avanzas y no estas agachado. */
public class SprintModule extends Module {
    public SprintModule() { super("Sprint auto", Category.UTILIDAD, Items.LEATHER_BOOTS, true); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) return;
        if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown()) {
            mc.options.keySprint.setDown(true);
        }
    }
}
