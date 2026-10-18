package kujama;

import net.minecraft.client.Minecraft;

/** Base de todos los modulos. */
public abstract class Module {
    public final String name;
    public boolean enabled;
    protected final Minecraft mc = Minecraft.getInstance();

    protected Module(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabled = enabledByDefault;
    }

    public void onTick() {}
    public void onEnable() {}
    public void onDisable() {}

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }
}
