package kujama;

import net.minecraft.world.item.Item;

/** Tarjeta de ajuste: solo guarda si esta activada o no (la usan otras funciones, como la intro). */
public class FlagModule extends Module {
    public FlagModule(String name, Item icon, Category category, boolean enabledByDefault) {
        super(name, category, icon, enabledByDefault);
    }
}
