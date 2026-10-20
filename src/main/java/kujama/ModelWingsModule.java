package kujama;

import net.minecraft.world.item.Item;

/** Tarjeta del menu para unas alas con forma (las dibuja KujamaWingsLayer). */
public class ModelWingsModule extends Module {
    public final int membraneRgb;
    public final int boneRgb;

    public ModelWingsModule(String name, Item icon, int membraneRgb, int boneRgb) {
        super(name, Category.VISUAL, icon, false);
        this.membraneRgb = membraneRgb;
        this.boneRgb = boneRgb;
    }

    @Override
    public void onEnable() {
        KujamaWingsLayer.current = this;
    }

    @Override
    public void onDisable() {
        if (KujamaWingsLayer.current != this) return;
        KujamaWingsLayer.current = null;
        for (Module m : ModuleManager.all()) {
            if (m != this && m.enabled && m instanceof ModelWingsModule other) {
                KujamaWingsLayer.current = other;
                return;
            }
        }
    }
}
